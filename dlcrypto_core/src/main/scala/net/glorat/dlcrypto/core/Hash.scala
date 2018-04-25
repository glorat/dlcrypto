package net.glorat.dlcrypto.core

/**
  * A cryptographic hash
  * @param inner underlying byte representation
  */
case class Hash(inner: Seq[Byte]) {
  def getBytes: Seq[Byte] = inner

  def toArray: Array[Byte] = {
    try {
      getBytes.toArray
    } catch {
      case e: Exception => throw e
    }

  }
  def toHex: String = bytesToHexString(inner.toArray)
  def toBase64: String = toArray.toBase64
  def toBase58: String = Base58.encode(toArray)
  override def toString: String = {
    toHex
  }
}

object Hash {
  def fromBase64(s: String): Hash = {
    Hash(net.glorat.dlcrypto.core.fromBase64(s))
  }
}


trait Hasher {
  def hash(data: Array[Byte]): Hash
}