package net.glorat.dlcrypto.core

import org.slf4j.Logger

private [this] trait Logging {
  lazy val log: Logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
}
