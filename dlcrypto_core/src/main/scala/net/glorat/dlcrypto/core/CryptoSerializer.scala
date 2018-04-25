package net.glorat.dlcrypto.core

/**
  * A hash safe serialized encoding of a DTO/value object case class
  */
trait CryptoSerializer {
  /**
    * A hash safe serialized encoding of a DTO/value object case class
    * @param form An immutable value object case class
    * @return Serialized form suitable to be hashed and signed
    */
  def forSign(form: Product): Array[Byte]

  /**
    * Returns length of forSign. Is likely to be much faster than actually trying to serialize
    * @param p An immutable value object case class
    * @return Length of forSign
    */
  def lengthOf(p: Product): Int
}
