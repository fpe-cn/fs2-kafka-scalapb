---
id: overview
title: Overview
---

Functional backpressured streams for consuming and producing Kafka records. Exposes a small interface, while taking care of common functionality: batch consuming and producing records, batched offset commits, offset commit recovery, and topic administration, while also simplifying client configuration.

Documentation is kept up-to-date, currently documenting v@LATEST_VERSION@ on Scala @DOCS_SCALA_MINOR_VERSION@.

## Getting Started

To get started with [sbt](https://scala-sbt.org), simply add the following line to your `build.sbt` file.

```scala
libraryDependencies += "@ORGANIZATION@" %% "@CORE_MODULE_NAME@" % "@LATEST_VERSION@"
```

Published for Scala @SCALA_PUBLISH_VERSIONS@. For changes, refer to the [release notes](https://github.com/fd4s/fs2-kafka/releases).

For Scala 2.12, enable partial unification by adding the following line to `build.sbt`.

```scala
scalacOptions += "-Ypartial-unification"
```

### Snapshot Releases

To use the latest snapshot release, add the following lines to your `build.sbt` file.

```scala
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "@ORGANIZATION@" %% "@CORE_MODULE_NAME@" % "@LATEST_SNAPSHOT_VERSION@"
```

## Dependencies

Refer to the table below for dependencies and version support across modules.

| Module                 | Dependencies                                                                                                                                                             | Scala                                |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------ |
| `@CORE_MODULE_NAME@`   | [ScalaPB @SCALAPB_VERSION@](https://github.com/scalapb/ScalaPB), [Confluent Kafka Avro Serializer @CONFLUENT_VERSION@](https://github.com/confluentinc/schema-registry) | Scala @CORE_CROSS_SCALA_VERSIONS@ |

## License

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html). Refer to the [license file](https://github.com/fd4s/fs2-kafka/blob/master/license.txt).
