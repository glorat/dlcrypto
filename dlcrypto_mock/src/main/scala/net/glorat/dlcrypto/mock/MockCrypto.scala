package net.glorat.dlcrypto.mock

import net.glorat.dlcrypto.core._
import java.security.{KeyPair, PrivateKey, PublicKey, SecureRandom}


object MockCryptoProvider extends SignerProvider {
  // Using this pattern to ensure clients don't see too much
  val signer: Signer = MockCrypto
}

object MockCrypto extends Signer {
  val secureRandom = new SecureRandom()

  def providerName: String = "Mock"

  protected def verify(message: Array[Byte], sig: Signature, identity: PublicKey): Boolean = {
    require(sig.isInstanceOf[MockSignature], "Signature must be a MockSignature")
    val idstr = new String(identity.getBytes)
    val sigstr = sig.asInstanceOf[MockSignature].sig
    sigstr.equals(s"Signed by $idstr")
  }

  def handlesPrivateKey(key:PrivateKey) : Boolean = key.isInstanceOf[MockPrivateKey]
  def handlesPublicKey(key:PublicKey) : Boolean = key.isInstanceOf[MockPublicKey]
  def publicKeyExtProvider(key: PublicKey): PublicKeyExtProvider = new MockPublicKeyProvider(key)

  def inferPublicKey(priv: PrivateKey): PublicKey = {
    new MockPublicKey(priv.asInstanceOf[MockPrivateKey].name)
  }

  def generate(): KeyPair = generate(secureRandom)

  def generate(rng: SecureRandom): KeyPair = {
    val ran = rng.nextInt() % 2
    val id = ran match {
      case 0 => "Ana"
      case 1 => "Barry"
      case _ => "Someone"
    }
    val priv = new MockPrivateKey(id)
    val pub = new MockPublicKey(id)
    new KeyPair(pub, priv)
  }

  def createVerificationKey(data: Array[Byte]): PublicKey = new MockPublicKey(new String(data))

  protected def sign(message: Array[Byte], key: PrivateKey): Signature = {
    val id = key match {
      case x : MockPrivateKey => x.name
      case _ => throw new IllegalArgumentException(s"Unknown key type ${key.getClass.getName}")
    }
    new MockSignature(s"Signed by $id")
  }

  def privateKeyExtProvider(key: PrivateKey): PrivateKeyExtProvider = new MockPrivateKeyProvider(key)

  def createSignature(data: Array[Byte]): Signature = new MockSignature(new String(data))

  def createSigningKey(data: Array[Byte]): PrivateKey = new MockPrivateKey(new String(data))
}

class MockPublicKey(val name:String) extends PublicKey {
  def getEncoded: Array[Byte] = name.getBytes("UTF-8")

  def getFormat: String = "Mock"

  def getAlgorithm: String = "Mock"
}

class MockPrivateKey(val name:String) extends PrivateKey {
  def getEncoded: Array[Byte] = name.getBytes("UTF-8")

  def getFormat: String = "Mock"

  def getAlgorithm: String = "Mock"
}

// Private Key
class MockPrivateKeyProvider(val s: PrivateKey) extends PrivateKeyExtProvider {
  def getBytes: Array[Byte] = s.getEncoded

  def toWIF: String = Base58.encode(getBytes)

  def toHex: String = getBytes.toHex

  def isCompressed: Boolean = false
}

class MockPublicKeyProvider(val p: PublicKey) extends PublicKeyExtProvider {
  def getBytes: Array[Byte] = p.getEncoded

  //noinspection NotImplementedCode
  def keyHash: Hash = ???

  def toAddress: Address = Address(Base58.encode(getBytes))

  def toHex: String = getBytes.toHex
}

class MockSignature(val sig:String) extends Signature {
  def getBytes: Array[Byte] = sig.getBytes("UTF-8")
}