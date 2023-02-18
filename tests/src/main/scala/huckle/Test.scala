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
