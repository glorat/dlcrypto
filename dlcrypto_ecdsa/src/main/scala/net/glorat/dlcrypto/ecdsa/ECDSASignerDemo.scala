package net.glorat.dlcrypto.ecdsa

import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import net.glorat.dlcrypto.core._
import net.glorat.dlcrypto.core.{Hash, Signer, Signature}

/**
  * This signer uses industry and java library standard encoding formats
  * of PKCS8 for private keys and X509 for public keys
  * These formats are larger than bitcoin formats
  *
  * This scheme isn't used anywhere to my knowledge so is kept here as reference and is not fully
  * implemented
  *
  * It remains useful since the code is clean and it shows how another signer can be
  * implemented
 */
object ECDSASignerDemo extends Signer {
  val providerName = "BC"
  val algorithmName = "ECDSA"

  private val secureRandom = new SecureRandom()
  private val keyFactory = KeyFactory.getInstance(algorithmName, providerName)

  def createSigningKey(data: Array[Byte]): PrivateKey = {
    val ks: PKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(data)
    val key: PrivateKey = keyFactory.generatePrivate(ks)
    key
  }

  def createVerificationKey(d: Array[Byte]): PublicKey = {
    val ks: X509EncodedKeySpec = new X509EncodedKeySpec(d)
    val key: PublicKey = keyFactory.generatePublic(ks)
    key
  }

  def createSignature(data: Array[Byte]): ByteSignature = {
    ByteSignature(data)
  }

  def verify(data: Array[Byte], sig: ByteSignature, identity: PublicKey): Boolean = {
    val s = Signature.getInstance(algorithmName, providerName)
    s.initVerify(identity)
    s.update(data)
    s.verify(sig.bytes)

  }
  def sign(data: Array[Byte], key: PrivateKey): ByteSignature = {
    val s = Signature.getInstance(algorithmName, providerName)
    s.initSign(key)
    s.update(data)
    val sig = s.sign()

    ByteSignature(sig)
  }
  def generate(): KeyPair = generate(secureRandom)

  def generate(rng: SecureRandom): KeyPair = {

    import java.security.spec.ECGenParameterSpec

    val ecGenSpec = new ECGenParameterSpec("secp256k1")
    val g = KeyPairGenerator.getInstance(algorithmName, providerName)
    g.initialize(ecGenSpec, rng)
    val pair = g.generateKeyPair()
    pair

  }

  // Private Key
  class MyKey(val s: PrivateKey) extends PrivateKeyExtProvider {
    def getBytes: Array[Byte] = s.getEncoded
    def toHex: String = bytesToHexString(getBytes)

    override def toWIF: String = ???

    override def isCompressed: Boolean = false
  }

  class MyPublicKey(val p: PublicKey) extends PublicKeyExtProvider {
    def getBytes: Array[Byte] = p.getEncoded
    def keyHash: Hash = {
      sha256hash160(getBytes)
    }

    def toHex: String = {
      bytesToHexString(getBytes)
    }

    override def toAddress: Address = ???
  }

  case class ByteSignature(data:Array[Byte]) extends Signature {
    override def getBytes: Array[Byte] = data
    def bytes:Array[Byte] = data
  }

  override protected def verify(message: Array[Byte], sig: Signature, identity: PublicKey): Boolean = ???

  // Ideally, check the concrete type in these two methods. Since this is just a sample,
  // return true and fail late during usage
  override def handlesPublicKey(key: PublicKey): Boolean = true

  override def handlesPrivateKey(key: PrivateKey): Boolean = true

  override def privateKeyExtProvider(key: PrivateKey): PrivateKeyExtProvider = new MyKey(key)

  override def publicKeyExtProvider(key: PublicKey): PublicKeyExtProvider = new MyPublicKey(key)

  override def inferPublicKey(priv: PrivateKey): PublicKey = throw new IllegalArgumentException("This signer cannot infer public keys")

}
