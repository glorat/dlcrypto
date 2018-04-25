package net.glorat.dlcrypto.mock

import net.glorat.dlcrypto.core.Signer
import net.glorat.dlcrypto.encode.Proto2Serializer

class MockSignerBehaviour extends net.glorat.dlcrypto.core.CryptoBehaviour()(Proto2Serializer) {
  def signer : Signer = MockCrypto
}
