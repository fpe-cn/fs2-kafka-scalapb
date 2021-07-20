/*
 * Copyright 2021-2021 Financiere des Paiements Electroniques
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package fs2.kafka.scalapb

import cats.effect.Sync
import cats.implicits._
import com.google.protobuf.Message
import fs2.kafka.{RecordSerializer, Serializer}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, JavaProtoSupport}

final class ProtobufSerializer[ScalaPB <: GeneratedMessage, JavaPB <: Message] private[scalapb] (
  private val companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]
) extends AnyVal {
  def using[F[_]](
    settings: ProtobufSettings[F, JavaPB]
  )(implicit F: Sync[F]): RecordSerializer[F, ScalaPB] = {
    val createSerializer: Boolean => F[Serializer[F, ScalaPB]] =
      settings.createProtobufSerializer(_).map { serializer =>
        Serializer.instance { (topic, _, a) =>
          F.pure(serializer.serialize(topic, companion.toJavaProto(a)))
        }
      }

    RecordSerializer.instance(
      forKey = createSerializer(true),
      forValue = createSerializer(false)
    )
  }

  override def toString: String =
    "ProtobufSerializer$" + System.identityHashCode(this)
}

object ProtobufSerializer {
  def apply[ScalaPB <: GeneratedMessage, JavaPB <: Message](
    implicit companion: GeneratedMessageCompanion[ScalaPB] with JavaProtoSupport[ScalaPB, JavaPB]
  ): ProtobufSerializer[ScalaPB, JavaPB] =
    new ProtobufSerializer(companion)
}
