package zio

import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class DeepAttemptBenchmark {
  case class ZIOError(message: String)

  @Param(Array("100"))
  var depth: Int = _

  def halfway: Int = depth / 2

  @Benchmark
  def futureDeepAttempt(): BigInt = {
    import scala.concurrent.Future
    import scala.concurrent.duration.Duration.Inf

    def descend(n: Int): Future[BigInt] =
      if (n == depth) Future.failed(new Exception("Oh noes!"))
      else if (n == halfway) descend(n + 1).recover { case _ => 50 }
      else descend(n + 1).map(_ + n)

    Await.result(descend(0), Inf)
  }

  @Benchmark
  def zioDeepAttemptBaseline(): BigInt = {
    def descend(n: Int): IO[Error, BigInt] =
      if (n == depth) IO.fail(new Error("Oh noes!"))
      else if (n == halfway) descend(n + 1).fold[BigInt](_ => 50, identity)
      else descend(n + 1).map(_ + n)

    runZio(descend(0))
  }

  @Benchmark
  def catsDeepAttempt(): BigInt = {
    import cats.effect._

    def descend(n: Int): IO[BigInt] =
      if (n == depth) IO.raiseError(new Error("Oh noes!"))
      else if (n == halfway) descend(n + 1).attempt.map(_.fold(_ => 50, a => a))
      else descend(n + 1).map(_ + n)

    runCatsEffect3(descend(0))
  }
}
