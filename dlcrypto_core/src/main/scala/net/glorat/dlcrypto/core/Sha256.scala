package net.glorat.dlcrypto.core

object Sha256Hasher extends Hasher {
  val ZERO_HASH : Hash = Hash(Seq.fill(32)(0) )
  
  def hash(data: Array[Byte]): Hash = {
    // Is this singleton? Thread safety???
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    Hash(digest.digest(data))
  }
}

object DoubleSha256Hasher extends Hasher {
  def hash(data: Array[Byte]): Hash = {
    // Is there some optimization possible in here??
    // The seq/array convert for starters
    Sha256Hasher.hash(Sha256Hasher.hash(data).getBytes.toArray)
  }
}
