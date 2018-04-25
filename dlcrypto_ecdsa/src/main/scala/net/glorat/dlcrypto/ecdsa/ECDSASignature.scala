package net.glorat.dlcrypto.ecdsa

import java.io.ByteArrayOutputStream
import java.math.BigInteger

import net.glorat.dlcrypto.core.Signature
import org.bouncycastle.asn1._
import org.bouncycastle.asn1.sec._
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.params.ECDomainParameters

case object ECDSASignature {
  val params: X9ECParameters = SECNamedCurves.getByName("secp256k1")
  val CURVE = new ECDomainParameters(params.getCurve, params.getG, params.getN, params.getH)
  val HALF_CURVE_ORDER: BigInteger = params.getN.shiftRight(1)

  /**
   * If decode cannot be performed, a zero signature is returned
   * This behaviour may be changed in future
   */
  def decodeFromDER(bytes: Array[Byte]): ECDSASignature = {

    try {
      val decoder = new ASN1InputStream(bytes)
      val seq = decoder.readObject().asInstanceOf[DLSequence]

      //val r = seq.getObjectAt(0).asInstanceOf[DERInteger]
      //val s = seq.getObjectAt(1).asInstanceOf[DERInteger]
      val r = seq.getObjectAt(0).asInstanceOf[ASN1Integer]
      val s = seq.getObjectAt(1).asInstanceOf[ASN1Integer]
      decoder.close()
      // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
      // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
      ECDSASignature(r.getPositiveValue, s.getPositiveValue)
    }
    catch {
      case _:Exception => ECDSASignature(BigInteger.ZERO,BigInteger.ZERO)
    }
  }

}

case class ECDSASignature(r: BigInteger, s: BigInteger) extends Signature {
  def getBytes: Array[Byte] = {
    encodeToDER
  }

  /**
   * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
   * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
   * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
   * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
   * considered legal and the other will be banned.
   */
  def ensureCanonical: ECDSASignature = {

    if (s.compareTo(ECDSASignature.HALF_CURVE_ORDER) > 0) {
      // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
      // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
      //    N = 10
      //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
      //    10 - 8 == 2, giving us always the latter solution, which is canonical.
      copy(s = ECDSASignature.CURVE.getN.subtract(s))
    } else {
      this
    }
  }

  /**
   * DER is an international standard for serializing data structures which is widely used in cryptography.
   * It's somewhat like protocol buffers but less convenient. This method returns a standard DER encoding
   * of the signature, as recognized by OpenSSL and other libraries.
   */
  def encodeToDER: Array[Byte] = {

    derByteStream.toByteArray

  }

  def derByteStream: ByteArrayOutputStream = {
    // Usually 70-72 bytes.
    val bos = new ByteArrayOutputStream(72)
    val seq = new DERSequenceGenerator(bos)
    seq.addObject(new ASN1Integer(r))
    seq.addObject(new ASN1Integer(s))
    seq.close()
    bos
  }
}