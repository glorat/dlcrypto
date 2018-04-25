package net.glorat.dlcrypto.core

import java.security.{PrivateKey, PublicKey}

case class PrivateWIF(wif: String)( implicit val signer:Signer) {
  lazy val privwifbytes: Seq[Byte] = Base58.decode(wif)
  lazy val compressed: Boolean = privwifbytes.size == 38
  lazy val (rest, checksum) = privwifbytes.splitAt(privwifbytes.size - 4)
  lazy val (prefix, unkbody) = rest.splitAt(1)
  lazy val (body, compressflag) = if (compressed) unkbody.splitAt(unkbody.size - 1) else (unkbody, Seq())
  lazy val privkey: PrivateKey = signer.createSigningKey(body.toArray)
  lazy val pubkey: PublicKey = signer.inferPublicKey(privkey)
  lazy val address: Address = pubkey.toAddress

  /** Checksum test */
  def isValid: Boolean = {
    val calcsum = DoubleSha256Hasher.hash(rest.toArray)
    ???
  }
}
