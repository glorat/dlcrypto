package net.glorat.dlcrypto.ecdsa

import javax.crypto.Cipher

import net.glorat.dlcrypto.core._
import net.glorat.dlcrypto.encode.Proto2Serializer
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.FixedSecureRandom
import org.scalatest._

case class Document(message:String)

class TestSigner extends FlatSpec with Logging {
  val signer = ECDSASigner
  implicit val encoder = Proto2Serializer

  val message = "This is the message to be signed"
  "ECDSASigner" should "roundtrip" in {
    val hasher = Sha256Hasher

    val bytes = message.getBytes()
    val hash = hasher.hash(bytes)
    log.info("Hash: {}", hash)
    val keyPair = signer.generate()
    val priv = keyPair.getPrivate; val pub = keyPair.getPublic
    log.info("Priv: {}", priv)
    log.info("PrivEnc: {}", priv.toHex)

    log.info("Pub: {}", pub)
    log.info("PubEnc: {}", pub.toHex)
    log.info("PubAdd: {}", pub.keyHash)
    val sig = signer.sign(hash, priv)
    log.info("Sig: {}", sig)
    val verified = signer.verify(hash, sig, pub)

    assert(verified)

    val keyPair2 = signer.generate()
    val priv2 = keyPair2.getPrivate; val pub2 = keyPair2.getPublic
    assert(false === signer.verify(hash, sig, pub2), "Wrong pub key")
    // Test key serialization to readable etc
    val pubstr = pub.toHex

    val pubround = signer.createVerificationKeyFromHex(pubstr)
    assert(pub.keyHash === pubround.keyHash) // Address format
    //assertEquals(pubround, pub)
    val privround = signer.createSigningKeyFromHex(priv.toHex)
    //assertEquals(Utils.bytesToHexString(privround.key.toByteArray), Utils.bytesToHexString(priv.key.toByteArray))
    assert(privround.toHex === priv.toHex)
    //assertEquals(privround, priv)
  }

  it should "roundtrip in reverse" in {
    val hasher = Sha256Hasher

    val bytes = message.getBytes()
    val hash = hasher.hash(bytes)
    log.info("Hash: {}", hash)
    val hashStr = "e807f8ea0828cc0195ab2319e413d1ab5731c090a818fc03a3b5ebc09aa3428a"

    /*s
    val (priv, pub) = ECDSASigner.generate
    log.info("Priv: {}", priv)
    log.info("Pub: {}", pub)

    */

    val k1 = Hex.decode("d5014e4b60ef2ba8b6211b4062ba3224e0427dd3")
    val k2 = Hex.decode("345e8d05c075c3a508df729a1685690e68fcfb8c8117847e89063bca1f85d968fd281540b6e13bd1af989a1fbf17e06462bf511f9d0b140fb48ac1b1baa5bded")

    val rng = new FixedSecureRandom(Array(k1, k2))
    val kp1 = signer.generate(rng)
    val pub = kp1.getPublic
    val priv = kp1.getPrivate
    val pubStr = pub.toHex
    val privStr = priv.toHex

    assert(pubStr === pub.toHex)
    assert(privStr === priv.toHex)
    assert(hashStr === hash.toString)

    log.info("Priv key: {}", priv)

    val sig = signer.sign(hash, priv)
    // ECDSA produces signatures with a random element to them!
    // assertEquals(sigStr, sig.toString)
    val verified = signer.verify(hash, sig, pub)

    assert(verified, "Sig should pass")

    val keyPair = signer.generate()
    val priv2 = keyPair.getPrivate; val pub2 = keyPair.getPublic
    assert(false===signer.verify(hash, sig, pub2), "Wrong pub key")
    // Test key serialization to readable etc
    val pubstr = pub.toHex
    val pubround = signer.createVerificationKeyFromHex(pubstr)
    assert(pubstr === pubround.toHex)
    val privround = signer.createSigningKeyFromHex(priv.toHex)
    assert(privround === priv)
    assert(privround === priv)
  }

  it should "sign and verify signatures" in {
    val pubStr = "0315110e3f89536fe539033ed0505e6ba31212170aa63ca2a74ccccf4290a1759c"
    val privStr = "acca5bdb170f96a243460823fbff3b0b80242dcae7159ecf84c747b801e3844e"
    val privKeyStr = "78155347028981525891795785974161919899825919487632156446187357113689869354062"

    val priv = signer.createSigningKeyFromHex(privStr)
    val pub = signer.createVerificationKeyFromHex(pubStr)
    // assertEquals(pub.keyHash.toString, "dd112706b0364bcd998d9b805ee67e2b33917e22")

    val msg = Document("Hello world")
    val sig = signer.sign(msg.getHash, priv)
    val sigBytes = sig.getBytes

    val isSigned = signer.verify(msg.getHash, sig, pub)

    assert(true === isSigned, "Signature should pass")

    val keyPair = signer.generate()
    val priv2 = keyPair.getPrivate; val pub2 = keyPair.getPublic
    val wrongSigned = signer.verify(msg.getHash, sig, pub2)

    assert(false === wrongSigned)

  }

  it should "sha256 hash" in {
    val bytes = message.getBytes()
    val hash = Sha256Hasher.hash(bytes)
    assert("e807f8ea0828cc0195ab2319e413d1ab5731c090a818fc03a3b5ebc09aa3428a" === hash.toString)
    assert("e807f8ea0828cc0195ab2319e413d1ab5731c090a818fc03a3b5ebc09aa3428a" === Sha256Hasher.hash(bytes).toString)

    //val addr = sha256hash160(bytes)
    //assertEquals("bf9d56638adcd7c51fd13547586aba411203b22d",addr.toString)
  }

  import java.math.BigInteger
  import java.security._
  import java.util.Date

  import org.bouncycastle.jce.X509Principal
  import org.bouncycastle.jce.provider.BouncyCastleProvider
  import org.bouncycastle.jce.spec.ECParameterSpec
  import org.bouncycastle.math.ec.ECCurve
  import org.bouncycastle.util.encoders.Hex
  import org.bouncycastle.x509.X509V3CertificateGenerator

  private def generateCertificate(keyPair: KeyPair) = {
    // ContentSigner sigGen = ...;

    val startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)

    val cert = new X509V3CertificateGenerator()
    cert.setSerialNumber(BigInteger.valueOf(1)) //or generate a random number
    cert.setSubjectDN(new X509Principal("CN=localhost")) //see examples to add O,OU etc
    cert.setIssuerDN(new X509Principal("CN=localhost")) //same since it is self-signed
    cert.setPublicKey(keyPair.getPublic)
    cert.setNotBefore(startDate)
    cert.setNotAfter(endDate)
    cert.setSignatureAlgorithm("SHA1withECDSA")
    val signingKey = keyPair.getPrivate
    cert.generate(signingKey, "BC")
  }

  "jce library" should "be installed with unlimited key strength" in {
    // https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
    // http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml
    val maxKeyLen : Int = Cipher.getMaxAllowedKeyLength("AES")
    // Oracle JDK default is 128. With unlimited policy, it should become Int.MaxValue
    assert(maxKeyLen >= 256, "Max key length must be at least 256. You need to install JCE")
  }

  it should "perform low level java crypto" in {

    Security.addProvider(new BouncyCastleProvider())

    val curve = new ECCurve.Fp(
      new BigInteger("883423532389192164791648750360308885314476597252960362792450860609699839"), // q
      new BigInteger("7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc", 16), // a
      new BigInteger("6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a", 16)) // b

    val ecSpec = new ECParameterSpec(
      curve,
      curve.decodePoint(Hex.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")), // G
      new BigInteger("883423532389192164791648750360308884807550341691627752275345424702807307")) // n
    val g = KeyPairGenerator.getInstance("ECDSA", "BC")

    g.initialize(ecSpec, new SecureRandom())

    val keyPair = g.generateKeyPair()

    val pubKey = keyPair.getPublic
    val privKey = keyPair.getPrivate

    val store = KeyStore.getInstance("BKS", "BC")
    store.load(null, null)
    val passwd = "hello world".toCharArray
    val chain: Array[java.security.cert.Certificate] = Array(generateCertificate(keyPair))
    // Error initialising store of key store: java.security.InvalidKeyException: Illegal key size
    // - then you need to enable unlimited key sizes
    store.setKeyEntry("entry", privKey, passwd, chain)

    val keyAgain = store.getKey("entry", passwd)

  }

  // This handy test is when the crypto classloading goes haywire with conflicting libs
  // Actually, the issue I had wasn't related to this but nonetheless, useful code to know about
  /* def testClasspath = {
    val classpath = new StringBuffer();
    var applicationClassLoader = ECDSASigner.getClass().getClassLoader();
    if (applicationClassLoader == null) {
      applicationClassLoader = ClassLoader.getSystemClassLoader();
    }
    val urls = (applicationClassLoader.asInstanceOf[URLClassLoader]).getURLs();
    urls.foreach(url => { classpath.append(url.getFile()).append("\r\n"); })

    println (classpath.toString())
  }
  */

}

trait Logging {
  lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass)
}
