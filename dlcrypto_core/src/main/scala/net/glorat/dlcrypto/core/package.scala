package net.glorat.dlcrypto

import java.security.{MessageDigest, PrivateKey, PublicKey}

import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.util.encoders.Base64

import scala.language.implicitConversions

package object core {
  private val refLock = new Object()

  // Will be populated with
  // ECDSASignerProvider.signer, MockCryptoProvider.signer
  // Via reflection
  private var allSigners : Seq[Signer] = Seq()
  private var registerDone = false

  import scala.reflect.runtime.{universe => ru}
  private val m = ru.runtimeMirror(getClass.getClassLoader)
  def registerSigner(clazz:String) : Unit = {
    try {
      val mod = m.staticModule(clazz)
      registerSigner(m.reflectModule(mod).instance.asInstanceOf[SignerProvider].signer)

    }
    catch {
      case _:Exception =>
        println (s"Failed to dyload ${clazz}")
        // throw e
    }

  }

  private def registerSigner(signer:Signer) : Unit = {
    println(s"Registering ${signer.getClass.getName}")
    require(!allSigners.contains(signer))
    allSigners = allSigners :+ signer
  }

  private def registerMe() = {
    refLock.synchronized {
      if (!registerDone) {
        registerSigner("net.glorat.dlcrypto.mock.MockCryptoProvider")
        registerSigner("net.glorat.dlcrypto.ecdsa.ECDSASignerProvider")
        registerDone = true
      }
    }

  }

  /**
   * Returns the given byte array hex encoded.
   */
  def bytesToHexString(bytes: Array[Byte]): String = {
    val buf = new StringBuffer(bytes.length * 2)
    for (b <- bytes) {
      val s = Integer.toString(0xFF & b, 16)
      if (s.length() < 2)
        buf.append('0')
      buf.append(s)
    }
    buf.toString
  }

  def fromBase64(x: String): Array[Byte] = {
    Base64.decode(x)
  }

  /**
   * Calculates RIPEMD160(SHA256(input)). This is used in Address calculations.
   */
  def sha256hash160(input: Array[Byte]): Hash = {
    val sha256: Array[Byte] = MessageDigest.getInstance("SHA-256").digest(input)
    val digest: RIPEMD160Digest = new RIPEMD160Digest
    digest.update(sha256, 0, sha256.length)
    val out: Array[Byte] = new Array[Byte](20)
    digest.doFinal(out, 0)
    Hash(out)

  }

  implicit class ByteArrayEnriched(x: Array[Byte]) {

    def toBase64: String = {
      new String(Base64.encode(x))
    }

    def toHex: String = {
      bytesToHexString(x)
    }
  }

  implicit class PrivateKeyExt(val s: PrivateKey) {
    registerMe()
    private val signer = allSigners.find(sg => sg.handlesPrivateKey(s)).getOrElse(throw new IllegalArgumentException(s"${s.getClass.getName} is an unknown private key type"))

    //private val signer = if (ECDSASigner.handlesPrivateKey(s)) ECDSASigner else throw new IllegalArgumentException(s"${s.getClass.getName} is an unknown private key type")
    def getBytes: Array[Byte] = signer.privateKeyExtProvider(s).getBytes
    def toHex: String = signer.privateKeyExtProvider(s).toHex
    def isCompressed:Boolean  = signer.privateKeyExtProvider(s).isCompressed
    def toWIF: String = signer.privateKeyExtProvider(s).toWIF
  }

  implicit class PublicKeyExt(val s: PublicKey) {
    registerMe()
    private val signer = allSigners.find(sg => sg.handlesPublicKey(s)).getOrElse(throw new IllegalArgumentException(s"${s.getClass.getName} is an unknown public key type"))

    //rivate val signer = if (ECDSASigner.handlesPublicKey(s)) ECDSASigner else throw new IllegalArgumentException(s"${s.getClass.getName} is an unknown public key type")
    def getBytes: Array[Byte] = signer.publicKeyExtProvider(s).getBytes
    def toHex: String = signer.publicKeyExtProvider(s).toHex
    def keyHash: Hash = signer.publicKeyExtProvider(s).keyHash
    def toAddress: Address = signer.publicKeyExtProvider(s).toAddress
  }


  implicit class CaseClassSerializer(val x: Product)(implicit chosenSerializer:CryptoSerializer) {
    /**
      * The serialized document payload
      */
    def forSign: Array[Byte] = {
      chosenSerializer.forSign(x)
    }

    def serializedLength : Int = chosenSerializer.lengthOf(x)

    /**
      * Hash of the document to be used for signing
      */
    def getHash: Hash = {
      Sha256Hasher.hash(forSign)
    }

  }

}
