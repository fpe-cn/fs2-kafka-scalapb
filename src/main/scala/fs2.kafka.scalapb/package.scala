/*
 * Copyright 2021-2021 Financiere des Paiements Electroniques
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package fs2.kafka

import _root_.scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}
import com.google.protobuf.Message

package object scalapb {

  /** Alias for `io.confluent.kafka.schemaregistry.client.SchemaRegistryClient`. */
  type SchemaRegistryClient =
    io.confluent.kafka.schemaregistry.client.SchemaRegistryClient

  /** Alias for `io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient`. */
  type CachedSchemaRegistryClient =
    io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient

  /** Alias for `io.confluent.kafka.serializers.KafkaProtobufDeserializer`. */
  type KafkaProtobufDeserializer[T <: Message] =
    io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer[T]

  /** Alias for `io.confluent.kafka.serializers.KafkaProtobufSerializer`. */
  type KafkaProtobufSerializer[T <: Message] =
    io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer[T]

  def protobufDeserializer[ScalaPB <: GeneratedMessage, JavaPB <: Message](
    implicit companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]
  ): ProtobufDeserializer[ScalaPB, JavaPB] =
    new ProtobufDeserializer(companion)

  def protobufSerializer[ScalaPB <: GeneratedMessage, JavaPB <: Message](
    implicit companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]
  ): ProtobufSerializer[ScalaPB, JavaPB] =
    new ProtobufSerializer[ScalaPB, JavaPB](companion)
}
