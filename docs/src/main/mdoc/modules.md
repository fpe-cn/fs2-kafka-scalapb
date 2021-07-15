---
id: modules
title: Modules
---

The following sections describe the additional modules.

## ScalaPB

The `@CORE_MODULE_NAME@` module provides [Protobuf](https://developers.google.com/protocol-buffers/) serialization support using [ScalaPB](https://scalapb.github.io/).

First, we have to configure ScalaPB as described in [ScalaPB installation guide](https://scalapb.github.io/docs/installation).
Do not forget to setup [Java conversions](https://scalapb.github.io/docs/sbt-settings#java-conversions) which are needed by the Confluent serializer.

After having configured ScalaPB, we start by creating a `.proto` file in `src/main/protobuf` containing the message we want to serialize.

```protobuf mdoc:reset-object
syntax = "proto3";

package fs2.kafka.test.Person;

message Person {
  string name = 1;
  int32 age = 2; 
}
```

We then define `ProtobufSettings`, describing the schema registry settings.

```scala
import cats.effect.IO
import fs2.kafka.scalapb._
import fs2.kafka.test.Person.PersonOuterClass
import fs2.kafka.ProducerSettings

val settings: ProtobufSettings[IO, PersonOuterClass.Person] =
  ProtobufSettings {
    SchemaRegistryClientSettings[IO]("http://localhost:8081")
      .withAuth(Auth.Basic("username", "password"))
  }
```

We can then create a `Serializer` and `Deserializer` instance for `Person`.

```scala
import cats.effect.IO
import fs2.kafka.scalapb._
import fs2.kafka.test.Person.person.Person
import fs2.kafka.{RecordDeserializer, RecordSerializer}

implicit val serializer: RecordSerializer[IO, Person] =
  protobufSerializer(Person).using(settings)

implicit val deserializer: RecordDeserializer[IO, Person] =
  protobufDeserializer(Person).using(settings)
```

Finally, we can create settings, passing the `Serializer`s and `Deserializer`s implicitly.

```scala
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, ProducerSettings}

val consumerSettings =
  ConsumerSettings[IO, String, Person]
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group")

val producerSettings =
  ProducerSettings[IO, String, Person]
    .withBootstrapServers("localhost:9092")
```

If we prefer, we can instead specificy the `Serializer`s and `Deserializer`s explicitly.

```scala
import fs2.kafka.{Deserializer, Serializer}

ConsumerSettings(
  keyDeserializer = Deserializer[IO, String],
  valueDeserializer = personDeserializer
).withAutoOffsetReset(AutoOffsetReset.Earliest)
 .withBootstrapServers("localhost:9092")
 .withGroupId("group")

ProducerSettings(
  keySerializer = Serializer[IO, String],
  valueSerializer = personSerializer
).withBootstrapServers("localhost:9092")
```

### Sharing Client

When creating `ProtobufSettings` with `SchemaRegistryClientSettings`, one schema registry client will be created per `Serializer` or `Deserializer`. For many cases, this is completely fine, but it's possible to reuse a single client for multiple `Serializer`s and `Deserializer`s.

To share a `SchemaRegistryClient`, we first create it and then pass it to `ProtobufSettings`.

```scala
val protobufSettingsSharedClient: IO[ProtobufSettings[IO]] =
  SchemaRegistryClientSettings[IO]("http://localhost:8081")
    .withAuth(Auth.Basic("username", "password"))
    .createSchemaRegistryClient
    .map(ProtobufSettings(_))
```

We can then create multiple `Serializer`s and `Deserializer`s using the `ProtobufSettings`.

```scala
protobufSettingsSharedClient.map { protobufSettings =>
  val personSerializer: RecordSerializer[IO, Person] =
    avroSerializer[Person].using(protobufSettings)

  val personDeserializer: RecordDeserializer[IO, Person] =
    avroDeserializer[Person].using(protobufSettings)

  val consumerSettings =
    ConsumerSettings(
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = personDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group")

 val producerSettings =
  ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = personSerializer
  ).withBootstrapServers("localhost:9092")

  (consumerSettings, producerSettings)
}
```
