/*
 * Copyright 2023 Arman Bilge
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

package huckle

import cats.Monoid
import cats.effect.IO

import scala.annotation.targetName

final case class Test private (test: IO[Unit])

object Test:
  @targetName("applyBoolean")
  def apply(name: String)(test: IO[Boolean]): Test =
    Test(name)(test.flatMap(x => IO(assert(x))))

  def apply(name: String)(test: IO[Unit]): Test =
    new Test(test.debug(name))

  given Monoid[Test] with
    def empty = Test(IO.unit)
    def combine(x: Test, y: Test) =
      Test(
        x.test.bothOutcome(y.test).flatMap { (xoc, yoc) =>
          xoc.embedError *> yoc.embedError
        },
      )
