package net.glorat.dlcrypto.core

/**
  * A bitcoin address
  * @param value string of the address
  */
case class Address(value: String) {
  override def toString: String = value
}
