# Vert.x Bitsy Integration

Integrates [Bitsy](https://bitbucket.org/lambdazen/bitsy) with Vert.x.

[![wercker status](https://app.wercker.com/status/5c403157d506664c07bfe3d2e5bf4d6b/m "wercker status")](https://app.wercker.com/project/bykey/5c403157d506664c07bfe3d2e5bf4d6b)

[![Maven](https://img.shields.io/maven-central/v/org.cstamas.vertx.bitsy/vertx-bitsy.svg)](https://repo1.maven.org/maven2/org/cstamas/vertx/bitsy/)

# Using it

To use Bitsy in Vert.x, you need to include following dependency to your project:

```
    <dependency>
      <groupId>org.cstamas.vertx.bitsy</groupId>
      <artifactId>vertx-bitsy</artifactId>
      <version>1.0.0</version>
    </dependency>

```

## Branches and building

* master - uses latest Bitsy 1.5.2 and Vert.x 3.3

Note: build depends on https://github.com/vert-x3/vertx-codegen/issues/81 as it uses Takari Lifecycle
with JDT compiler! Hence, must use Vert.x 3.3.2+ if you are building this project.


Have fun!  
~t~
