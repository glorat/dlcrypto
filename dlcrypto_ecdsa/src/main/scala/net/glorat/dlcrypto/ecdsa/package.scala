package net.glorat.dlcrypto

import net.glorat.dlcrypto.ecdsa.ECDSASignerProvider
import net.glorat.dlcrypto.core.Signer

package object ecdsa {
  lazy val ECDSASigner : Signer = ECDSASignerProvider.signer
}
