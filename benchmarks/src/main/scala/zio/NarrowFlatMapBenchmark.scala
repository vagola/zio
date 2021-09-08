package zio

import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class NarrowFlatMapBenchmark {
  @Param(Array("100"))
  var size: Int = _

  @Benchmark
  def futureNarrowFlatMap(): Int = {
    import scala.concurrent.Future
    import scala.concurrent.duration.Duration.Inf

    def loop(i: Int): Future[Int] =
      if (i < size) Future(i + 1).flatMap(loop)
      else Future(i)

    Await.result(Future(0).flatMap(loop), Inf)
  }

  @Benchmark
  def zioNarrowFlatMap(): Int = {
    def loop(i: Int): UIO[Int] =
      if (i < size) IO.succeed[Int](i + 1).flatMap(loop)
      else IO.succeed(i)

    runZio(IO.succeed(0).flatMap[Any, Nothing, Int](loop))
  }

  @Benchmark
  def catsNarrowFlatMap(): Int = {
    import cats.effect._

    def loop(i: Int): IO[Int] =
      if (i < size) IO(i + 1).flatMap(loop)
      else IO(i)

    runCatsEffect3(IO(0).flatMap(loop))
  }
}
