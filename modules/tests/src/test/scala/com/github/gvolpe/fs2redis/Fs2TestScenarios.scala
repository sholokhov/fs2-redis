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

package com.github.gvolpe.fs2redis

import cats.effect.IO
import com.github.gvolpe.fs2redis.effects._
import cats.implicits._
import com.github.gvolpe.fs2redis.interpreter.Fs2Redis
import io.lettuce.core.GeoArgs

trait Fs2TestScenarios {

  def locationScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] = {
    val _BuenosAires  = GeoLocation(Longitude(-58.3816), Latitude(-34.6037), "Buenos Aires")
    val _RioDeJaneiro = GeoLocation(Longitude(-43.1729), Latitude(-22.9068), "Rio de Janeiro")
    val _Montevideo   = GeoLocation(Longitude(-56.164532), Latitude(-34.901112), "Montevideo")
    val _Tokyo        = GeoLocation(Longitude(139.6917), Latitude(35.6895), "Tokyo")

    val testKey = "location"
    for {
      _ <- cmd.geoAdd(testKey, _BuenosAires)
      _ <- cmd.geoAdd(testKey, _RioDeJaneiro)
      _ <- cmd.geoAdd(testKey, _Montevideo)
      _ <- cmd.geoAdd(testKey, _Tokyo)
      x <- cmd.geoDist(testKey, _BuenosAires.value, _Tokyo.value, GeoArgs.Unit.km)
      _ <- IO { assert(x == 18374.9052) }
      y <- cmd.geoPos(testKey, _RioDeJaneiro.value)
      _ <- IO { assert(y.contains(GeoCoordinate(-43.17289799451828, -22.906801071586663))) }
      z <- cmd.geoRadius(testKey, GeoRadius(_Montevideo.lon, _Montevideo.lat, Distance(10000.0)), GeoArgs.Unit.km)
      _ <- IO { assert(z.toList.containsSlice(List(_BuenosAires.value, _Montevideo.value, _RioDeJaneiro.value))) }
    } yield ()
  }

  def hashesScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] = {
    val testKey   = "foo"
    val testField = "bar"
    for {
      x <- cmd.hGet(testKey, testField)
      _ <- IO { assert(x.isEmpty) }
      isSet1 <- cmd.hSetNx(testKey, testField, "some value")
      _ <- IO { assert(isSet1) }
      y <- cmd.hGet(testKey, testField)
      _ <- IO { assert(y.contains("some value")) }
      isSet2 <- cmd.hSetNx(testKey, testField, "should not happen")
      _ <- IO { assert(!isSet2) }
      w <- cmd.hGet(testKey, testField)
      _ <- IO { assert(w.contains("some value")) }
      _ <- cmd.hDel(testKey, testField)
      z <- cmd.hGet(testKey, testField)
      _ <- IO { assert(z.isEmpty) }
    } yield ()
  }

  def listsScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] = {
    val testKey = "listos"
    for {
      t <- cmd.lRange(testKey, 0, 10)
      _ <- IO { assert(t.isEmpty) }
      _ <- cmd.rPush(testKey, "one", "two", "three")
      x <- cmd.lRange(testKey, 0, 10)
      _ <- IO { assert(x == List("one", "two", "three")) }
      y <- cmd.lLen(testKey)
      _ <- IO { assert(y.contains(3)) }
      a <- cmd.lPop(testKey)
      _ <- IO { assert(a.contains("one")) }
      b <- cmd.rPop(testKey)
      _ <- IO { assert(b.contains("three")) }
      z <- cmd.lRange(testKey, 0, 10)
      _ <- IO { assert(z == List("two")) }
    } yield ()
  }

  def setsScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] = {
    val testKey = "foos"
    for {
      x <- cmd.sMembers(testKey)
      _ <- IO { assert(x.isEmpty) }
      _ <- cmd.sAdd(testKey, "set value")
      y <- cmd.sMembers(testKey)
      _ <- IO { assert(y.contains("set value")) }
      o <- cmd.sCard(testKey)
      _ <- IO { assert(o == 1L) }
      _ <- cmd.sRem("non-existing", "random")
      w <- cmd.sMembers(testKey)
      _ <- IO { assert(w.contains("set value")) }
      _ <- cmd.sRem(testKey, "set value")
      z <- cmd.sMembers(testKey)
      _ <- IO { assert(z.isEmpty) }
      t <- cmd.sCard(testKey)
      _ <- IO { assert(t == 0L) }
    } yield ()
  }

  def sortedSetsScenario(cmd: Fs2Redis.RedisCommands[IO, String, Long]): IO[Unit] = {
    val testKey = "zztop"
    for {
      t <- cmd.zRevRangeByScore(testKey, ZRange(0, 2), limit = None)
      _ <- IO { assert(t.isEmpty) }
      _ <- cmd.zAdd(testKey, args = None, ScoreWithValue(Score(1), 1), ScoreWithValue(Score(3), 2))
      x <- cmd.zRevRangeByScore(testKey, ZRange(0, 2), limit = None)
      _ <- IO { assert(x == List(1)) }
      y <- cmd.zCard(testKey)
      _ <- IO { assert(y.contains(2)) }
      z <- cmd.zCount(testKey, ZRange(0, 1))
      _ <- IO { assert(z.contains(1)) }
    } yield ()
  }

  def stringsScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] = {
    val key = "test"
    for {
      x <- cmd.get(key)
      _ <- IO { assert(x.isEmpty) }
      isSet1 <- cmd.setNx(key, "some value")
      _ <- IO { assert(isSet1) }
      y <- cmd.get(key)
      _ <- IO { assert(y.contains("some value")) }
      isSet2 <- cmd.setNx(key, "should not happen")
      _ <- IO { assert(!isSet2) }
      isSet3 <- cmd.mSetNx(Map("multikey1" -> "someVal1", "multikey2" -> "someVal2"))
      _ <- IO { assert(isSet3) }
      isSet4 <- cmd.mSetNx(Map("multikey1" -> "someVal0", "multikey3" -> "someVal3"))
      _ <- IO { assert(!isSet4) }
      val1 <- cmd.get("multikey1")
      _ <- IO { assert(val1.contains("someVal1")) }
      val3 <- cmd.get("multikey3")
      _ <- IO { assert(val3.isEmpty) }
      isSet5 <- cmd.mSetNx(Map("multikey1" -> "someVal1", "multikey2" -> "someVal2"))
      _ <- IO { assert(!isSet5) }
      w <- cmd.get(key)
      _ <- IO { assert(w.contains("some value")) }
      _ <- cmd.del(key)
      z <- cmd.get(key)
      _ <- IO { assert(z.isEmpty) }
    } yield ()
  }

  def stringsClusterScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] = {
    val key = "test"
    for {
      x <- cmd.get(key)
      _ <- IO { assert(x.isEmpty) }
      isSet1 <- cmd.setNx(key, "some value")
      _ <- IO { assert(isSet1) }
      y <- cmd.get(key)
      _ <- IO { assert(y.contains("some value")) }
      isSet2 <- cmd.setNx(key, "should not happen")
      _ <- IO { assert(!isSet2) }
      w <- cmd.get(key)
      _ <- IO { assert(w.contains("some value")) }
      _ <- cmd.del(key)
      z <- cmd.get(key)
      _ <- IO { assert(z.isEmpty) }
    } yield ()
  }

  def connectionScenario(cmd: Fs2Redis.RedisCommands[IO, String, String]): IO[Unit] =
    for {
      pong <- cmd.ping
      _ <- IO { assert(pong === "PONG") }
    } yield ()

}
