package net.glorat.dlcrypto.core

trait Signature {
  def getBytes: Array[Byte]
  override def toString: String = {
    bytesToHexString(getBytes)
  }
}
