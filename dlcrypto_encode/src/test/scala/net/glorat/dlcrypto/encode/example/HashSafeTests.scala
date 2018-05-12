package net.glorat.dlcrypto.encode.example

import net.glorat.dlcrypto.core.Logging
import net.glorat.dlcrypto.encode.Proto2Serializer
import org.scalatest.FlatSpec


//Let's keep code short so...
//noinspection NameBooleanParameters
class HashSafeTests extends FlatSpec {
  import net.glorat.dlcrypto.encode.HashSafeValidator._

  case class StringTuple(s1:String, s2:String)
  case class StringTupleAgain(s1:String, s2:String)
  case class StringTriple(s1:String, s2:String, s3:String)
  case class IntTuple(i1:Int, i2:Int)

  "Basic value object" should "pass the rules" in {
    validate(StringTuple("abc", "abc"), StringTuple("abc","abc"), true)
    validate(StringTuple("abc", "abc"), StringTuple("123","123"), false)
    // Some encoding will have these the same
    validate(StringTuple("abc", "abc"), StringTuple("ab","cabc"), false)
  }

  "Structurally equivalent value objects" should "equal" in {
    validate(StringTuple("abc","abc"), StringTupleAgain("abc", "abc"), true)
    validate(StringTuple("abc", "abc"), StringTupleAgain("123","123"), false)
  }

  "Structurally non-equivalent" should "not equal" in {
    // Different arity
    validate(StringTuple("abc", "abc"), StringTriple("abc","abc",""), false)
    // Same arity, different types
    validate(StringTuple("abc", "abc"), IntTuple(1,2), false)
  }

  case class NestingObject(s1:String, st: StringTuple)
  "Value objects containing other value objects" should "follow rules" in {
    val o1 = NestingObject("abc", StringTuple("abc","bcd"))
    val o2 = NestingObject("abc", StringTuple("abc","bcd"))
    validate(o1,o2,true)

  }

  case class HasCollection(s1:String, matrix: Seq[Int])
  "Seq of non-bytes" should "not be supported yet" in {
    val o1 = HasCollection("a", Seq(1,2))

    assertThrows[IllegalArgumentException] {
      validate(o1)
    }
    assertThrows[IllegalArgumentException] {
      Proto2Serializer.forSign(o1)
    }
  }

  case class HasByteArray(b:Seq[Byte])
  "Seq of Byte" should "validate" in {
    val ba = HasByteArray(Seq(1,2,3))
    validate(ba)
  }

  case class HasUuid(uuid:java.util.UUID)
  "UUID" should "follow rules" in {
    val o1 = HasUuid(java.util.UUID.randomUUID())
    // Even try to create a new ref!
    val o2 = HasUuid(new java.util.UUID(o1.uuid.getMostSignificantBits, o1.uuid.getLeastSignificantBits))
    validate(o1, o2, true)
  }
}
