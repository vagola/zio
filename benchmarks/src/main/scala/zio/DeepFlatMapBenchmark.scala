package zio

import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class DeepFlatMapBenchmark {
  @Param(Array("3", "10"))
  var depth: Int = _

  @Benchmark
  def futureBroadFlatMap(): BigInt = {
    import scala.concurrent.Future
    import scala.concurrent.duration.Duration.Inf

    def fib(n: Int): Future[BigInt] =
      if (n <= 1) Future(n)
      else
        fib(n - 1).flatMap(a => fib(n - 2).flatMap(b => Future(a + b)))

    Await.result(fib(depth), Inf)
  }

  @Benchmark
  def zioBroadFlatMap(): BigInt = {
    def fib(n: Int): UIO[BigInt] =
      if (n <= 1) ZIO.succeed[BigInt](n)
      else
        fib(n - 1).flatMap(a => fib(n - 2).flatMap(b => ZIO.succeed(a + b)))

    runZio(fib(depth))
  }

  @Benchmark
  def catsBroadFlatMap(): BigInt = {
    import cats.effect._

    def fib(n: Int): IO[BigInt] =
      if (n <= 1) IO(n)
      else
        fib(n - 1).flatMap(a => fib(n - 2).flatMap(b => IO(a + b)))

    runCatsEffect3(fib(depth))
  }
}
