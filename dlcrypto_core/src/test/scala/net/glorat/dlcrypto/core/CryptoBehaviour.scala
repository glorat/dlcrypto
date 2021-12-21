package net.glorat.dlcrypto.core

import org.scalatest.flatspec.AnyFlatSpec

import java.security.KeyPair

abstract class CryptoBehaviour(implicit val chosenSerializer:CryptoSerializer) extends AnyFlatSpec
{
  private case class MyDocument(msg:String)

  def signer : Signer

  "Signer" should "have a provider name" in new {
    assert(!signer.providerName.isEmpty)
  }

  it should "be able to generate a keypair" in new {
    val keyPair: KeyPair = signer.generate()
  }

  it should "be able to sign and verify a document" in new {
    val keyPair: KeyPair = signer.generate()
    val doc = MyDocument("Hello world")
    val sig: Signature = signer.sign(doc.getHash, keyPair.getPrivate)
    val verify: Boolean = signer.verify(doc.getHash, sig, keyPair.getPublic)
    assert(verify === true)
  }


}
