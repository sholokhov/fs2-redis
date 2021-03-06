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

import com.github.gvolpe.fs2redis.domain.DefaultRedisCodec
import org.scalatest.FunSuite

class Fs2RedisClusterSpec extends FunSuite with RedisClusterTest with Fs2TestScenarios {

  test("cluster: geo api")(withRedisCluster(locationScenario))

  test("cluster: hashes api")(withRedisCluster(hashesScenario))

  test("cluster: lists api")(withRedisCluster(listsScenario))

  test("cluster: sets api")(withRedisCluster(setsScenario))

  test("cluster: sorted sets api")(
    withAbstractRedisCluster[Unit, String, Long](sortedSetsScenario)(DefaultRedisCodec(LongCodec))
  )

  test("cluster: strings api")(withRedisCluster(stringsClusterScenario))

  test("cluster: connection api")(withRedisCluster(connectionScenario))

}
