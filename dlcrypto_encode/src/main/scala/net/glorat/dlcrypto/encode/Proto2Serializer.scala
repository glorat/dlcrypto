package net.glorat.dlcrypto.encode

import java.nio.ByteBuffer

import net.glorat.dlcrypto.core.{Address, CryptoSerializer, Hash}
import com.google.protobuf.{ByteString, CodedOutputStream}

/**
  * This class supports a variant of the protobuf proto2 serialization format
  *
  * The purpose is to always have a well defined round-trip serialization format
  * that is safe for the use of cryptographic signing and is also cross-platform
  * and cross-language
  *
  * It restricts proto2 by
  * - Mandating everything is required
  *
  * It does not yet support proto2 features such as
  * - Arrays (except for byte arrays)
  * - Anything remotely complex
  *
  * It extends proto2 by providing custom serializers for
  * - UUID
  * - Hash
  */
object Proto2Serializer extends CryptoSerializer {
  def forSign(form: Product): Array[Byte] = {
    val a = new Array[Byte](lengthOf(form))
    val os = CodedOutputStream.newInstance(a)
    write(form.asInstanceOf[Product], os)
    os.checkNoSpaceLeft()
    a
  }

  def uuidToBytes(uuid:java.util.UUID) : ByteString = {
    val bb: ByteBuffer = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    ByteString.copyFrom(bb.array)
  }

  def lengthOf(p: Product): Int = {
    import com.google.protobuf.CodedOutputStream._

    var ret : Int = 0
    var tag = 0
    p.productIterator.foreach(item => {
      tag += 1
      val s: Int = item match {
        case x: Int => /*if (x!=0)*/  computeInt32Size(tag, x) //else 0
        case x: Hash => computeBytesSize(tag, ByteString.copyFrom(x.getBytes.toArray))
        case x: java.util.UUID => computeBytesSize(tag, uuidToBytes(x))
        case x: String => computeStringSize(tag, x)
        case x: Address => computeStringSize(tag, x.value)
        case x: Seq[_] if x.isEmpty => 0
        case x: Seq[_] if x.head.isInstanceOf[Byte] => computeBytesSize(tag, ByteString.copyFrom(x.asInstanceOf[Seq[Byte]].toArray))
        case _: Seq[_] => throw new IllegalArgumentException("Only Seq[Byte] supported")
        case x: Product => 1 + computeUInt32SizeNoTag(lengthOf(x)) + lengthOf(x)
      }
      ret += s
    })

    ret
  }

  def write(x: Product, os: com.google.protobuf.CodedOutputStream): Unit = {
    var tag : Int = 0
    x.productIterator.foreach(x => {
      tag += 1
      x match {
        case x: Int => /* proto3 if (x!=0)*/ os.writeInt32(tag, x)
        case x: Hash =>
          os.writeByteArray(tag, x.getBytes.toArray)
        case x: java.util.UUID =>
          os.writeBytes(tag, uuidToBytes(x))
        case x: String =>
          // proto3
          //if (x != "") {
          os.writeString(tag, x)
          //}
        case x:Address =>
          os.writeString(tag, x.value)
        case x: Seq[_] => writeArray(x, tag, os)
        case x: Product =>
          os.writeTag(tag, 2) // Packed array of 1
          os.writeUInt32NoTag(Proto2Serializer.lengthOf(x))
          write(x, os)
      }
    })
  }
  def writeArray(seq: Seq[_], tag: Int, os: com.google.protobuf.CodedOutputStream): Unit = {
    if (seq.nonEmpty) {
      seq.head match {
        case x: Byte =>
          os.writeBytes(tag, ByteString.copyFrom(seq.asInstanceOf[Seq[Byte]].toArray))
        case _ => throw new IllegalArgumentException("Only serializing Seq[Byte]")
      }
    }
  }
}
