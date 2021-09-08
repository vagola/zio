package zio

import cats.effect.{IO => CIO}
import zio.internal._

import scala.concurrent.ExecutionContext

object BenchmarkUtil extends Runtime[ZEnv] {

  val catsEffectRuntime = cats.effect.unsafe.implicits.global

  val environment = Runtime.default.environment
  val runtimeConfig = RuntimeConfig
    .makeDefault(1024)
    .withTracing(Tracing.disabled)

  implicit val futureExecutionContext: ExecutionContext = ExecutionContext.global

  def catsRepeat[A](n: Int)(io: CIO[A]): CIO[A] =
    if (n <= 1) io
    else io.flatMap(_ => catsRepeat(n - 1)(io))

  def runCatsEffect3[A](io: cats.effect.IO[A]): A =
    (CIO.cede.flatMap(_ => io)).unsafeRunSync()(catsEffectRuntime)

  def runZio[E, A](zio: => ZIO[Any, E, A]): A =
    unsafeRun(ZIO.yieldNow.flatMap(_ => zio))

}
