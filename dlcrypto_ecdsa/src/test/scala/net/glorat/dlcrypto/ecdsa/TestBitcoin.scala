package net.glorat.dlcrypto.ecdsa

import java.io.ByteArrayOutputStream

import net.glorat.dlcrypto.ecdsa
import net.glorat.dlcrypto.core._
import org.scalatest._

class TestBitcoin extends FlatSpec with Logging {
  val message = "This is the message to be signed"

  implicit val signer : Signer = ECDSASigner

  "Bitcoin" should "decode base58" in {
    // The non-checked variants
    assert("deadbeef" === Hash(Base58.decode("6h8cQN")).toHex)
  }

  it should "handle WIF keys" in {
    // This example from https://en.bitcoin.it/wiki/Wallet_import_format

    val privwif = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ"
    val privwifbytes: Seq[Byte] = Base58.decode(privwif)
    assert(37 === privwifbytes.size)
    val compressed = privwifbytes.size == 38

    val (rest, checksum) = privwifbytes.splitAt(privwifbytes.size - 4)
    val (prefix, body) = rest.splitAt(1)
    // Do a checksum test on "rest"
    val calcsum = DoubleSha256Hasher.hash(rest.toArray)
    assert("507a5b8d" === calcsum.toHex.take(8))
    assert("80" === bytesToHexString(prefix.toArray))

    val privkey = signer.createSigningKey(body.toArray)
    assert("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d" === privkey.toHex)

    // Infer the public key
    val pubkey = signer.inferPublicKey(privkey)
    // The commented out lines are the compressed form
    //assertEquals("02d0de0aaeaefad02b8bdc8a01a1b8b11c696bd3d66a2c5f10780d95b7df42645c", pubkey.toHex)
    //assertEquals("d9351dcbad5b8f3b8bfa2f2cdc85c28118ca9326", pubkey.keyHash.toHex)
    assert("04d0de0aaeaefad02b8bdc8a01a1b8b11c696bd3d66a2c5f10780d95b7df42645cd85228a6fb29940e858e7e55842ae2bd115d1ed7cc0e82d934e929c97648cb0a" === pubkey.toHex)
    assert("a65d1a239d4ec666643d350c7bb8fc44d2881128" === pubkey.keyHash.toHex)
  }

  // Ignoring because we have lost support for compressed keys
  ignore should "decode compressed keys" in  {
    // val privwif = PrivateWIF("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ") // Uncompressed equivalent
    val privwif = PrivateWIF("KwdMAjGmerYanjeui5SHS7JkmpZvVipYvB2LJGU1ZxJwYvP98617")
    assert(true === privwif.compressed)
    assert("d9351dcbad5b8f3b8bfa2f2cdc85c28118ca9326" === privwif.pubkey.keyHash.toHex)

    //            00-D9351DCBAD5B8F3B8BFA2F2CDC85C28118CA9326-31FCAAD6

    assert(Address("1LoVGDgRs9hTfTNJNuXKSpywcbdvwRXpmK") === privwif.pubkey.toAddress)
  }

  it should "write WIF" in {
    val privkey = signer.createSigningKeyFromHex("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d")
    val str = privkey.toWIF
    assert("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ" === str)
  }

  // Ignoring due to lost support for compressed keys
  ignore should "read default WIF" in  {
    val privwif = PrivateWIF("L4rK1yDtCWekvXuE6oXD9jCYfFNV2cWRpVuPLBcCU2z8TrisoyY1")

    assert(privwif.compressed)

    val privexp = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    assert(privexp === privwif.privkey.toHex)

    assert("9a1c78a507689f6f54b847ad1cef1e614ee23f1e" === privwif.pubkey.keyHash.toHex)

    val addr = privwif.pubkey.toAddress
    assert(Address("1F3sAm6ZtwLAUnj7d38pGFxtP3RVEvtsbV") === addr)
    
    val pubhex = privwif.pubkey.getBytes.toHex
    assert("03a34b99f22c790c4e36b2b3c2c35a36db06226e41c692fc82b8b56ac1c540c5bd" === pubhex)

  }

  it should "handle invalid WIF" in {
    val privwif = PrivateWIF("1")
    intercept[IllegalArgumentException] {
      privwif.privkey
      privwif.pubkey
    }

  }

  ignore should "wrife WIF in steps" in {
    val privkey = signer.createSigningKeyFromHex("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d")

    val bo = new ByteArrayOutputStream
    bo.write(0x80) // Prefix
    bo.write(privkey.getBytes)
    val soFar = bo.toByteArray
    assert("800c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d" === bytesToHexString(soFar))

    // Checksum is first 4 bytes of Sha256(Sha256(soFar))
    val checkSum = DoubleSha256Hasher.hash(soFar).getBytes.take(4)
    assert("507a5b8d" === bytesToHexString(checkSum.toArray))
    bo.write(checkSum.toArray)
    // Finally base58 encode it
    val last = bo.toByteArray
    assert("800C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D507A5B8D" === bytesToHexString(last).toUpperCase())
    val str = Base58.encode(last)
    assert("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ" === str)

  }

  it should "create brainwallet keys" in {
    val emptyStr= "" // The "default" brainwallet key with an empty passphrase
    val hash = Sha256Hasher.hash(emptyStr.getBytes("UTF-8"))
    val key = signer.createSigningKey(hash.getBytes.toArray) // bouncy castle goes uncompressed
    //val wif = key.toWIF
    //assertEquals(wif, "L4rK1yDtCWekvXuE6oXD9jCYfFNV2cWRpVuPLBcCU2z8TrisoyY1")
    val wif2 = key.toWIF
    assert(wif2 === "5KYZdUEo39z3FPrtuX2QbbwGnNP5zTd7yyr2SC1j299sBCnWjss")
  }

}