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

package com.github.gvolpe.fs2redis.algebra

import com.github.gvolpe.fs2redis.effects.{ RangeLimit, ScoreWithValue, ZRange }
import io.lettuce.core.{ ZAddArgs, ZStoreArgs }

trait SortedSetCommands[F[_], K, V] extends SortedSetGetter[F, K, V] with SortedSetSetter[F, K, V]

trait SortedSetGetter[F[_], K, V] {
  def zCard(key: K): F[Option[Long]]
  def zCount(key: K, range: ZRange[V])(implicit ev: Numeric[V]): F[Option[Long]]
  def zLexCount(key: K, range: ZRange[V]): F[Option[Long]]
  def zRange(key: K, start: Long, stop: Long): F[List[V]]
  def zRangeByLex(key: K, range: ZRange[V], limit: Option[RangeLimit]): F[List[V]]
  def zRangeByScore(key: K, range: ZRange[V], limit: Option[RangeLimit])(implicit ev: Numeric[V]): F[List[V]]
  def zRangeByScoreWithScores(key: K, range: ZRange[V], limit: Option[RangeLimit])(
      implicit ev: Numeric[V]
  ): F[List[ScoreWithValue[V]]]
  def zRangeWithScores(key: K, start: Long, stop: Long): F[List[ScoreWithValue[V]]]
  def zRank(key: K, value: V): F[Option[Long]]
  def zRevRange(key: K, start: Long, stop: Long): F[List[V]]
  def zRevRangeByLex(key: K, range: ZRange[V], limit: Option[RangeLimit]): F[List[V]]
  def zRevRangeByScore(key: K, range: ZRange[V], limit: Option[RangeLimit])(implicit ev: Numeric[V]): F[List[V]]
  def zRevRangeByScoreWithScores(key: K, range: ZRange[V], limit: Option[RangeLimit])(
      implicit ev: Numeric[V]
  ): F[List[ScoreWithValue[V]]]
  def zRevRangeWithScores(key: K, start: Long, stop: Long): F[List[ScoreWithValue[V]]]
  def zRevRank(key: K, value: V): F[Option[Long]]
  def zScore(key: K, value: V): F[Option[Double]]
}

trait SortedSetSetter[F[_], K, V] {
  def zAdd(key: K, args: Option[ZAddArgs], values: ScoreWithValue[V]*): F[Unit]
  def zAddIncr(key: K, args: Option[ZAddArgs], value: ScoreWithValue[V])(implicit ev: Numeric[V]): F[Unit]
  def zIncrBy(key: K, member: V, amount: Double): F[Unit]
  def zInterStore(destination: K, args: Option[ZStoreArgs], keys: K*): F[Unit]
  def zRem(key: K, values: V*): F[Unit]
  def zRemRangeByLex(key: K, range: ZRange[V]): F[Unit]
  def zRemRangeByRank(key: K, start: Long, stop: Long): F[Unit]
  def zRemRangeByScore(key: K, range: ZRange[V])(implicit ev: Numeric[V]): F[Unit]
  def zUnionStore(destination: K, args: Option[ZStoreArgs], keys: K*): F[Unit]
}
