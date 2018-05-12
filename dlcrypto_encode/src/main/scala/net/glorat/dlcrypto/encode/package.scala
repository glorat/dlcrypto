package net.glorat.dlcrypto

import java.nio.ByteBuffer

import com.google.protobuf.ByteString
import com.trueaccord.scalapb.TypeMapper
import net.glorat.dlcrypto.core.{Address, Hash}

package object encode {
  private val btohash :(ByteString=>Hash) = x => Hash(x.toByteArray)
  private val hashtob : (Hash =>ByteString) = x => ByteString.copyFrom(x.toArray)
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