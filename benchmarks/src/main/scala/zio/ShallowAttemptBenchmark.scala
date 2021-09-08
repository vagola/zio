package zio

import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ShallowAttemptBenchmark {
  case class ZIOError(message: String)

  @Param(Array("100"))
  var depth: Int = _

  @Benchmark
  def futureShallowAttempt(): BigInt = {
    import scala.concurrent.Future
    import scala.concurrent.duration.Duration.Inf

    def throwup(n: Int): Future[BigInt] =
      if (n == 0) throwup(n + 1) recover { case _ => 0 }
      else if (n == depth) Future(1)
      else
        throwup(n + 1).recover { case _ => 0 }
          .flatMap(_ => Future.failed(new Exception("Oh noes!")))

    Await.result(throwup(0), Inf)
  }

  @Benchmark
  def zioShallowAttempt(): BigInt = {
    def throwup(n: Int): IO[ZIOError, BigInt] =
      if (n == 0) throwup(n + 1).fold[BigInt](_ => 50, identity)
      else if (n == depth) IO.succeed(1)
      else throwup(n + 1).foldZIO[Any, ZIOError, BigInt](_ => IO.succeedNow(0), _ => IO.fail(ZIOError("Oh noes!")))

    runZio(throwup(0))
  }

  @Benchmark
  def catsShallowAttempt(): BigInt = {
    import cats.effect._

    def throwup(n: Int): IO[BigInt] =
      if (n == 0) throwup(n + 1).attempt.map(_.fold(_ => 0, a => a))
      else if (n == depth) IO(1)
      else
        throwup(n + 1).attempt.flatMap {
          case Left(_)  => IO(0)
          case Right(_) => IO.raiseError(new Error("Oh noes!"))
        }

    runCatsEffect3(throwup(0))
  }
}
