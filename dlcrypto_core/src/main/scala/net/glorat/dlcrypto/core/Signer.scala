package net.glorat.dlcrypto.core

import java.security.{KeyPair, PrivateKey, PublicKey, SecureRandom}

import org.bouncycastle.util.encoders.Hex

/**
  * The core interface of this library, providing type safety on the crypto functions as well as
  * enriching the various types with extended methods to make writing crypto applications more natural
  */
trait Signer {
  // Slight reluctance to expose this but seems needed for wallet impls
  def providerName : String

  protected def verify(message: Array[Byte], sig: Signature, identity: PublicKey): Boolean
  protected def sign(message: Array[Byte], key: PrivateKey): Signature
  def generate(): KeyPair
  def generate(rng: SecureRandom): KeyPair

  def handlesPublicKey(key:PublicKey) : Boolean
  def handlesPrivateKey(key:PrivateKey) : Boolean

  def privateKeyExtProvider(key: PrivateKey): PrivateKeyExtProvider
  def publicKeyExtProvider(key: PublicKey): PublicKeyExtProvider

  def createSigningKey(data: Array[Byte]): PrivateKey
  def createVerificationKey(data: Array[Byte]): PublicKey
  def inferPublicKey(priv: PrivateKey): PublicKey
  def createSignature(data: Array[Byte]): Signature

  // Trait helpers
  def createSigningKeyFromHex(key: String): PrivateKey = createSigningKey(Hex.decode(key))
  def createVerificationKeyFromHex(key: String): PublicKey = createVerificationKey(Hex.decode(key))
  def sign(data: Hash, key: PrivateKey): Signature = sign(data.getBytes.toArray, key)
  def sign(data: Hash, keyBytes: Array[Byte]): Signature = sign(data.getBytes.toArray, createSigningKey(keyBytes))
  def signToBytes(data: Hash, keyBytes: Array[Byte]): Array[Byte] = sign(data, keyBytes).getBytes

  def verify(data: Hash, sig: Seq[Byte], identity: Seq[Byte]): Boolean = {
    val sig2 = createSignature(sig.toArray)
    val key = createVerificationKey(identity.toArray)
    verify(data.toArray, sig2, key)
  }
  def verify(data: Hash, sig: Signature, identity: PublicKey): Boolean = {
    verify(data.toArray, sig, identity)
  }
}

trait SignerProvider {
  def signer:Signer
}

trait PrivateKeyExtProvider {
  def getBytes: Array[Byte]
  def toHex: String
  def toWIF: String
  def isCompressed : Boolean
}

trait PublicKeyExtProvider {
  def getBytes: Array[Byte]
  def keyHash: Hash
  def toHex: String
  def toAddress: Address
}
