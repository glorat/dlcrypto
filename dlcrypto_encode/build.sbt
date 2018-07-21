name := "dlcrypto_encode"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)