cmf
===

credential management framework

This framework provides a number of services that allow for password and credential management.
This initial version provides rudimentary password indirection/aliasing capabilities built on top of a JCEKS keystore.

See the CMFTest class for tests of the provided aliasing functionality.
See the CMFBootstrapService class as an example for how to spin up the framework.
A dependency injection framework may be worth using instead of the bootstrap service.
