package net.glorat.dlcrypto.ecdsa

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.{Signature => _, Signer => _, _}
import java.security.spec.ECGenParameterSpec

import net.glorat.dlcrypto.core._
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.params.{ECDomainParameters, ECPrivateKeyParameters, ECPublicKeyParameters}
import org.bouncycastle.jcajce.provider.asymmetric.ec.{BCECPrivateKey, BCECPublicKey}
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.{BouncyCastleProvider, JCEECPrivateKey, JCEECPublicKey}
import org.bouncycastle.jce.spec.{ECNamedCurveParameterSpec, ECPrivateKeySpec, ECPublicKeySpec}

import scala.math.BigInt.javaBigInteger2bigInt

object ECDSASignerProvider extends SignerProvider {

  // Need to handle classloader recycling... assume just one
  if (java.security.Security.getProvider("BC") != null) {
    println("bitbit.crypto removing/readding provider in static initialiser. New classloader?")
    java.security.Security.removeProvider("BC")
  }
  private val bcprov = new BouncyCastleProvider()
  println("Registering provider" + bcprov.getClass.getClassLoader)
  java.security.Security.addProvider(bcprov)

  // Using this pattern to ensure clients don't see too much
  val signer: Signer = ECDSASignerBitcoin

}

/**
  * This signer attempts to be Bitcoin compatible in its encoding formats
  *
  * It also attempts to be code compatible with different java Security providers to some extent
  * but this library has only been tested successfully with bouncycastle, since its code is
  * open
  *
  * The code gets hairy compared to vanilla java.Security to Bitcoin specials like
  * - Compressed keys
  * - Custom binary encoding
  */
private object ECDSASignerBitcoin extends Signer {
  val providerName = "BC"
  val algorithmName = "ECDSA"
  val ecGenSpec = new ECGenParameterSpec("secp256k1")
  val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1")

  val params: X9ECParameters = SECNamedCurves.getByName("secp256k1")
  private val ecDomainParameters = new ECDomainParameters(params.getCurve, params.getG, params.getN, params.getH)

  private val secureRandom = new SecureRandom()
  private val generator = KeyPairGenerator.getInstance(algorithmName, providerName)
  private val keyFactory = KeyFactory.getInstance(algorithmName, providerName)

  def handlesPublicKey(key: PublicKey): Boolean = {
    key.isInstanceOf[BCECPublicKey] || key.isInstanceOf[JCEECPublicKey] || key.isInstanceOf[ECPublicKey]
  }

  def handlesPrivateKey(key: PrivateKey): Boolean = {
    key.isInstanceOf[BCECPrivateKey] || key.isInstanceOf[JCEECPrivateKey]
  }

  def privateKeyExtProvider(priv: PrivateKey) = new MyPrivateKey(priv)

  def publicKeyExtProvider(pub: PublicKey) = new SigningKey(pub)

  def createSigningKey(data: Array[Byte]): PrivateKey = {
    val bigInt = BigInt(1, data).bigInteger

    val priKeySpec = new ECPrivateKeySpec(
      bigInt, ecSpec)

    try {
      keyFactory.generatePrivate(priKeySpec)

    } catch {
      case e: Exception if e.toString.contains("key spec not recognised") => {
        // This should never happen in theory
        // but it happened a lot in practice during early development
        // Get some diagnostics...
        val actual = fromWhere(priKeySpec)
        val expected = fromWhere(ECDSASignerBitcoin.generate(secureRandom).getPrivate)

        println(s"My supposedly bad key spec comes from $actual")
        println(s"Expected source was $expected")
        // Chuck it back
        throw e
      }
    }

  }

  private def fromWhere(o: Any): String = {
    require(o != null)
    val c = o.getClass
    var loader = c.getClassLoader
    if (loader == null) {
      // Try the bootstrap classloader - obtained from the ultimate parent of the System Class Loader.
      loader = ClassLoader.getSystemClassLoader
      while (loader != null && loader.getParent != null) {
        loader = loader.getParent
      }
    }

    if (loader != null) {
      val name = c.getCanonicalName
      val resource = loader.getResource(name.replace(".", "/") + ".class")
      if (resource != null) {
        resource.toString
      } else {
        "Unknown - no resource"
      }
    } else {
      "Unknown - no loader"
    }

  }

  def createVerificationKey(pub: Array[Byte]): PublicKey = {
    val pt = try {
      ecDomainParameters.getCurve.decodePoint(pub)
    } catch {
      case _: ArrayIndexOutOfBoundsException => throw new IllegalArgumentException(s"Invalid public key: ${bytesToHexString(pub)}")
      case e: Exception => throw new Exception(s"Unknown issue with public key: ${bytesToHexString(pub)}", e)
    }
    val foo = new ECPublicKeySpec(pt, ecSpec)
    keyFactory.generatePublic(foo)
  }

  override def inferPublicKey(priv: PrivateKey): PublicKey = {
    val d = priv match {
      case pkey: BCECPrivateKey => pkey.getD
      case pkey: JCEECPrivateKey => pkey.getD
      case _ => throw new Exception("Don't recognise private key type " + priv.getClass)
    }
    if (d == BigInteger.ZERO) throw new IllegalArgumentException("PrivateKey invalid")
    val q = params.getG.multiply(d)
    val spec = new ECPublicKeySpec(q, ecSpec)
    keyFactory.generatePublic(spec)
  }

  def createSignature(data: Array[Byte]): Signature = {
    ECDSASignature.decodeFromDER(data)
  }

  protected def verify(data: Array[Byte], sig: Signature, identity: PublicKey): Boolean = {
    sig match {
      case signature: ECDSASignature if identity.isInstanceOf[BCECPublicKey] =>
        val pubkey = identity.asInstanceOf[BCECPublicKey]
        val pubpt = pubkey.getQ
        val signer = new org.bouncycastle.crypto.signers.ECDSASigner()
        val params = new ECPublicKeyParameters(ecDomainParameters.getCurve.decodePoint(pubkey.getBytes), ecDomainParameters)
        signer.init(false, params)
        try {
          signer.verifySignature(data, signature.r, signature.s)
        } catch {
          case e: Exception => {
            // Just in case bouncy castle dies?!
            println("Caught NPE inside bouncy castle")
            e.printStackTrace()
            false
          }

        }

      case _ =>
        false
    }
    /*
    val s = java.security.Signature.getInstance(algorithmName, providerName)
    s.initVerify(identity)
    s.update(data)
    s.verify(sig.getBytes)*/

  }

  protected def sign(data: Array[Byte], priv: PrivateKey): ECDSASignature = {
    /* ASN flavour 
    val s = java.security.Signature.getInstance(algorithmName, providerName);
    s.initSign(key)
    s.update(data)
    ByteSignature(sig)
    * 
    */
    val d = priv match {
      case pkey: BCECPrivateKey => pkey.getD
      case pkey: JCEECPrivateKey => pkey.getD
      case _ => throw new Exception("Don't recognise private key type " + priv.getClass)
    }

    val signer = new org.bouncycastle.crypto.signers.ECDSASigner()
    val privKey = new ECPrivateKeyParameters(d, ecDomainParameters)
    signer.init(true, privKey)
    val components = signer.generateSignature(data)
    val x = ECDSASignature(components(0), components(1)).ensureCanonical
    x
  }

  def generate(): KeyPair = generate(secureRandom)

  def generate(rng: SecureRandom): KeyPair = {
    val g = KeyPairGenerator.getInstance(algorithmName, providerName)
    g.initialize(ecGenSpec, rng)
    val pair = g.generateKeyPair()
    pair
  }

  // Private Key
  class MyPrivateKey(val s: PrivateKey) extends PrivateKeyExtProvider {
    def getBytes: Array[Byte] = {
      val d = s match {
        case pkey: BCECPrivateKey => pkey.getD
        case pkey: JCEECPrivateKey => pkey.getD
        case _ => throw new Exception("Don't recognise private key type " + s.getClass)
      }
      bigIntegerToBytes(d, 32)
    }

    def isCompressed: Boolean = {
      val d = s match {
        case pkey: BCECPrivateKey => pkey.getParameters.getG.isCompressed
        case pkey: JCEECPrivateKey => pkey.getParameters.getG.isCompressed
        case _ => throw new Exception("Don't recognise private key type " + s.getClass)
      }
      d
    }

    def toHex: String = bytesToHexString(getBytes)

    def toWIF: String = {
      val compressed = this.isCompressed
      // https://en.bitcoin.it/wiki/Base58Check_encoding
      val bo = new ByteArrayOutputStream
      bo.write(Base58.Prefix.SecretKey) // Prefix
      bo.write(getBytes)

      if (compressed) {
        val suf: Byte = 1
        bo.write(suf)
      }
      val soFar = bo.toByteArray
      // Checksum is first 4 bytes of Sha256(Sha256(soFar))
      val checkSum = DoubleSha256Hasher.hash(soFar).getBytes.take(4)
      bo.write(checkSum.toArray)
      // Finally base58 encode it
      Base58.encode(bo.toByteArray())
    }
  }

  class SigningKey(val p: PublicKey) extends PublicKeyExtProvider {
    def getBytes: Array[Byte] = {
      val q = p match {
        //org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
        case pkey: BCECPublicKey => pkey.getQ
        case pkey: JCEECPublicKey => pkey.getQ
        case pkey: ECPublicKey => pkey.getQ
        //        case pkey: ECPublicKey => pkey.getW().
        case _ => throw new Exception("Don't recognise public key type " + p.getClass)
      }

      //val compressed = compressPoint(q)
      //val ret = compressed.getEncoded()
      q.getEncoded(false) // No compression
    }

    /** Gets the hash160 form of the public key (as seen in addresses). */
    def keyHash: Hash = {
      sha256hash160(getBytes)
    }

    def toHex: String = {
      bytesToHexString(getBytes)
    }

    /** Create the address based on the uncompressed standard */
    def toAddress: Address = {
      // https://en.bitcoin.it/wiki/Base58Check_encoding
      val bo = new ByteArrayOutputStream
      bo.write(Base58.Prefix.PubkeyAddress) // Bitcoin pubkey hash
      bo.write(keyHash.toArray)
      val soFar = bo.toByteArray
      // Checksum is first 4 bytes of Sha256(Sha256(soFar))
      val checkSum = DoubleSha256Hasher.hash(soFar).getBytes.take(4)
      bo.write(checkSum.toArray)
      // Finally base58 encode it
      Address(Base58.encode(bo.toByteArray()))
    }
  }

  /*
    * Bitcoin deviates from the obvious java.math.BigInteger.toByteArray() in terms of leading zeroes
    * and padding
    */
  private def bigIntegerToBytes(b: BigInt, numBytes: Int): Array[Byte] = {
    val bytes = Array.ofDim[Byte](numBytes)
    val biBytes = b.toByteArray
    val start = if (biBytes.length == numBytes + 1) 1 else 0
    val length = Math.min(biBytes.length, numBytes)
    System.arraycopy(biBytes, start, bytes, numBytes - length, length)
    bytes
  }
}
