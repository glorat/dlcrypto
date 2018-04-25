package net.glorat.dlcrypto.ecdsa

import net.glorat.dlcrypto.core.{CryptoBehaviour, Signer}
import net.glorat.dlcrypto.encode.Proto2Serializer

class ECSDASignerBehaviour extends CryptoBehaviour()(Proto2Serializer) {
  def signer : Signer = ECDSASignerProvider.signer
}
