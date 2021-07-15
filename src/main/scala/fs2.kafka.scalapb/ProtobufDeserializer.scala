/*
 * Copyright 2021-2021 Financiere des Paiements Electroniques
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package fs2.kafka.scalapb

import _root_.scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}
import cats.effect.Sync
import cats.implicits._
import com.google.protobuf.Message
import fs2.kafka.{Deserializer, RecordDeserializer}

final class ProtobufDeserializer[ScalaPB <: GeneratedMessage, JavaPB <: Message] private[scalapb] (
  private val companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]
) extends AnyVal {
  def using[F[_]](
    settings: ProtobufSettings[F, JavaPB]
  )(implicit F: Sync[F]): RecordDeserializer[F, ScalaPB] = {
    val createDeserializer: Boolean => F[Deserializer[F, ScalaPB]] =
      settings.createProtobufDeserializer(_).map { deserializer =>
        Deserializer.instance { (topic, _, bytes) =>
          F.defer {
            F.delay(deserializer.deserialize(topic, bytes)).map(companion.fromJavaProto)
          }
        }
      }

    RecordDeserializer.instance(
      forKey = createDeserializer(true),
      forValue = createDeserializer(false)
    )
  }

  override def toString: String =
    "ProtobufDeserializer$" + System.identityHashCode(this)
}

object ProtobufDeserializer {
  def apply[ScalaPB <: GeneratedMessage, JavaPB <: Message](
    implicit companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]
  ): ProtobufDeserializer[ScalaPB, JavaPB] =
    new ProtobufDeserializer(companion)
}
