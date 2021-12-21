package net.glorat.dlcrypto

import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.protobuf.ByteString
import net.glorat.dlcrypto.core.{Address, Hash}
import scalapb.TypeMapper

package object encode {
  private val btohash :(ByteString=>Hash) = x => Hash(x.toByteArray)
  private val hashtob : (Hash =>ByteString) = x => ByteString.copyFrom(x.toArray)
  val isoDateToDate: (String=>LocalDate) = x => LocalDate.parse(x, DateTimeFormatter.ISO_LOCAL_DATE)
  val dateToIsoDate: (LocalDate=>String) = x => x.format(DateTimeFormatter.ISO_LOCAL_DATE)

  implicit val localDateMapper: TypeMapper[String, java.time.LocalDate]
  = TypeMapper(isoDateToDate)(dateToIsoDate)

  implicit val hashMapper : TypeMapper[ByteString,Hash] = TypeMapper(btohash)(hashtob)
  implicit val uuidMapper: TypeMapper[ByteString, java.util.UUID] = TypeMapper(Util.bytesToUuid)(Util.uuidToBytes)
  implicit val bytesMapper: TypeMapper[ByteString,Seq[Byte]] = TypeMapper(Util.scalaToGoogleBytes)(Util.googleToScalaBytes)
  implicit val addressMapper: TypeMapper[String, Address] = TypeMapper(Address.apply)(_.value)

}

// Conversions for ScalaPB
private[this] object Util {
  def uuidToBytes(uuid: java.util.UUID) : ByteString = {
    val bb: ByteBuffer = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)

    ByteString.copyFrom(bb.array)
  }

  def bytesToUuid(bytes:ByteString) : java.util.UUID = {
    if (bytes == _root_.com.google.protobuf.ByteString.EMPTY){
      // Handle creation of default instance
      new java.util.UUID(0,0)
    }
    else {
      val b = bytes.asReadOnlyByteBuffer()
      val l1 = b.getLong
      val l2 = b.getLong
      new java.util.UUID(l1, l2)
    }
  }

  def googleToScalaBytes(bytes:Seq[Byte]) : ByteString = {
    ByteString.copyFrom(bytes.toArray)
  }

  def scalaToGoogleBytes(goog: ByteString) : Seq[Byte] = {
    goog.toByteArray.toSeq
  }
}