dlcrypto [![Build Status][travis-image]][travis-url]
========

A collection of Scala libraries and classes that wrap cryptographic functions as used in
Bitcoin plus other useful functions into a set of high level type safe interfaces.

Those types are useful as building blocks for creating PKI or blockchain
based applications

Included libraries are
- dlcrypto_core - All the core type safe interfaces used across the libraries
- dlcrypto_ecdsa - A public key signing/verification implementation based on
ECDSA and Bitcoin's specific flavour of it
- dlcrypto_encode - An encoding library suitable for cryptographic
hashing of Scala DTO
- dlcrypto_mock - A mock public key library suitable for testing

For more information of each, please see the individual README.md for each
library

Pre-requisites
--------------
JCE Unlimited Policy files *must* be enabled. See your Java
documentation on how to do this. Otherwise, you will see tests fail

Aside from that pre-requisites are obvious. The author has been primarily
developing against Java 8. Build system is based on sbt


```
Copyright 2018 Kevin Tam
```
[travis-image]: https://travis-ci.org/glorat/dlcrypto.svg?branch=master
[travis-url]: https://travis-ci.org/glorat/dlcrypto