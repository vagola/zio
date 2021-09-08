package zio

import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.annotation.tailrec

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class MapBenchmark {
  @Param(Array("10"))
  var depth: Int = _

  @Benchmark
  def futureMap(): BigInt = {
    import scala.concurrent.duration.Duration.Inf
    import scala.concurrent.{Await, Future}

    @tailrec
    def sumTo(t: Future[BigInt], n: Int): Future[BigInt] =
      if (n <= 1) t
      else sumTo(t.map(_ + n), n - 1)

    Await.result(sumTo(Future(0), depth), Inf)
  }

  @Benchmark
  def zioMap(): BigInt = {
    @tailrec
    def sumTo(t: UIO[BigInt], n: Int): UIO[BigInt] =
      if (n <= 1) t
      else sumTo(t.map(_ + n), n - 1)

    runZio(sumTo(IO.succeed(0), depth))
  }

  @Benchmark
  def catsMap(): BigInt = {
    import cats.effect._

    @tailrec
    def sumTo(t: IO[BigInt], n: Int): IO[BigInt] =
      if (n <= 1) t
      else sumTo(t.map(_ + n), n - 1)

    runCatsEffect3(sumTo(IO(0), depth))
  }
}
