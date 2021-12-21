package net.glorat.dlcrypto.encode

import scalapb.GeneratedMessage

import java.time.LocalDate


/**
  * Created by kevin on 21/4/2018.
  */
object HashSafeValidator {
  def valueEquals(p1:Any, p2:Any): Boolean = {

    p1 match {
      case product: Product if p2.isInstanceOf[Product] => valueEquals(product, p2.asInstanceOf[Product])
      case dt: LocalDate => {
        // LocalDate and string equivalence
        valueEquals(dateToIsoDate(dt), p2)
      }
      case s1:String => {
        p2 match {
          case s2:String => s1 == s2
          case dt2:LocalDate => valueEquals(p1, dateToIsoDate(dt2))
          case _ => false
        }
      }
      case _ =>
        // Aside from products, must be same type and be immutable and equal
        // Hard to check all of that so go for simple equals
        p1 == p2
    }
  }

  def valueEquals(p1:Product, p2:Product): Boolean = {
    // First the simple case if the types are same
    if (p1.getClass == p2.getClass)
      p1 == p2
    else {
      // If types are different, valueEquals still passes if the types are structurally the same
      if (p1.productArity == p2.productArity) {
        val elemEqs = (0 until p1.productArity).map(i => valueEquals(p1.productElement(i), p2.productElement(i)))
        elemEqs.forall(x => x)
      }
      else {
        false
      }
    }
  }

  /**
    * Validates that the two objects pass the hash safe test for encoding
    * - Must be case classes
    * - Must be value object case classes (currently not checked)
    *    - Must only contain immutable types, or immutable collections of immutable types or other value objects
    * - Encoding of two value objects are equal IFF two value objects are equal
    * @param o1 first object to compare
    * @param o2 second object to compare
    */
  def validate(o1:Object, o2:Object) :Unit = {
    if (!o1.isInstanceOf[Product]) throw new IllegalArgumentException("o1 must be a case class")
    if (!o2.isInstanceOf[Product]) throw new IllegalArgumentException("o2 must be a case class")
    validate(o1.asInstanceOf[Product], o2.asInstanceOf[Product])
  }

  def validate(o1:Object, o2:Object, expectedEquals:Boolean) :Unit = {
    val veq = valueEquals(o1,o2)
    require(veq == expectedEquals, s"Value equality $veq but expected $expectedEquals")
    validate(o1, o2)
  }

  /**
    * Performs necessary but not sufficient checks that the argument
    * can be serialized
    * @param o1 object to check
    */
  def validate(o1:Any) : Unit = {
    if (!o1.isInstanceOf[Product]) throw new IllegalArgumentException("o1 must be a case class")
    val p1 = o1.asInstanceOf[Product]
    val p1encode = Proto2Serializer.forSign(p1)
    require(p1encode.length == Proto2Serializer.lengthOf(p1), "Proto2 should encode to expected length")
  }

  /**
    * A stronger test for proto2 generated classes from scalapb that
    * the serialization used for hashing is the same as message passing
    * @param o1 object to check
    */
  def validateMore(o1:GeneratedMessage) : Unit = {
    if (!o1.isInstanceOf[Product]) throw new IllegalArgumentException("o1 must be a case class")
    val pbbytes : Seq[Byte] = o1.toByteArray.toSeq
    val mybytes : Seq[Byte] = Proto2Serializer.forSign(o1.asInstanceOf[Product])
    require(pbbytes == mybytes,
      "dlcrypto serialization should match proto2 spec. This may fail if you are using types not supported by dlcrypto - for example, the types may not be safe")
  }

  private def validate(p1:Product, p2:Product) : Unit = {
    val veqs = valueEquals(p1,p2)
    // Call toSeq so that we can check equality by array value (not ref)
    val p1encode = Proto2Serializer.forSign(p1).toSeq
    val p2encode = Proto2Serializer.forSign(p2).toSeq
    val encodeEquals = p1encode == p2encode
    require(veqs == encodeEquals, s"(p1 == p2) == $veqs but encoded equality is $encodeEquals")

    require(p1encode.length == Proto2Serializer.lengthOf(p1), s"Proto2 should encode p1 to predicted length")
    require(p2encode.length == Proto2Serializer.lengthOf(p2), s"Proto2 should encode p2 to predicted length")
  }

}
