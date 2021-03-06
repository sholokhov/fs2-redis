/*
 * Copyright 2018-2019 Fs2 Redis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gvolpe.fs2redis.interpreter.pubsub

import cats.effect.{ Concurrent, ContextShift, Sync }
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.github.gvolpe.fs2redis.algebra.PubSubStats
import com.github.gvolpe.fs2redis.domain._
import com.github.gvolpe.fs2redis.streams.Subscription
import com.github.gvolpe.fs2redis.effect.JRFuture
import fs2.Stream
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection

import scala.collection.JavaConverters._

class Fs2PubSubStats[F[_]: Concurrent: ContextShift, K, V](
    pubConnection: StatefulRedisPubSubConnection[K, V]
) extends PubSubStats[Stream[F, ?], K] {

  override def pubSubChannels: Stream[F, List[K]] =
    Stream
      .eval {
        JRFuture(Sync[F].delay(pubConnection.async().pubsubChannels()))
      }
      .map(_.asScala.toList)

  override def pubSubSubscriptions(channel: Fs2RedisChannel[K]): Stream[F, Subscription[K]] =
    pubSubSubscriptions(List(channel)).map(_.headOption).unNone

  override def pubSubSubscriptions(channels: List[Fs2RedisChannel[K]]): Stream[F, List[Subscription[K]]] =
    Stream.eval {
      for {
        kv <- JRFuture(Sync[F].delay(pubConnection.async().pubsubNumsub(channels.map(_.value): _*)))
        rs <- Sync[F].delay(kv.asScala.toList.map { case (k, n) => Subscription(DefaultChannel[K](k), n) })
      } yield rs
    }

}
