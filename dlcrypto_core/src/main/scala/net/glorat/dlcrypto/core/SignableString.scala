package net.glorat.dlcrypto.core

/**
  * A bitcoin signable string
  * @param string
  */
case class SignableString(string: String) extends AnyVal {
  def toHash: Hash = {
    val bo = new java.io.ByteArrayOutputStream()
    val os = new java.io.DataOutputStream(bo)
    os.write(SignableString.SALT_BYTES)
    // TODO: Add a Int/VarInt header?
    os.write(string.getBytes("UTF-8"))
    bo.close()
    val data = bo.toByteArray

    Sha256Hasher.hash(data)
  }
}

object SignableString {
  val SALT = "Signed Message:\n"
  // TODO: Add a Int/VarInt header?
  val SALT_BYTES: Array[Byte] = SALT.getBytes("UTF-8")
}
