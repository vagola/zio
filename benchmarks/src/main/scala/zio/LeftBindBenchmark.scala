package zio

import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.concurrent.Await

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LeftBindBenchmark {
  @Param(Array("100"))
  var size: Int = _

  @Param(Array("10"))
  var depth: Int = _

  @Benchmark
  def futureLeftBindBenchmark(): Int = {
    import scala.concurrent.Future
    import scala.concurrent.duration.Duration.Inf

    def loop(i: Int): Future[Int] =
      if (i % depth == 0) Future(i + 1).flatMap(loop)
      else if (i < size) loop(i + 1).flatMap(i => Future(i))
      else Future(i)

    Await.result(Future(0).flatMap(loop), Inf)
  }

  @Benchmark
  def zioLeftBindBenchmark: Int = {
    def loop(i: Int): UIO[Int] =
      if (i % depth == 0) IO.succeed[Int](i + 1).flatMap(loop)
      else if (i < size) loop(i + 1).flatMap(i => IO.succeed(i))
      else IO.succeed(i)

    runZio(IO.succeed(0).flatMap[Any, Nothing, Int](loop))
  }

  @Benchmark
  def catsLeftBindBenchmark(): Int = {
    import cats.effect._

    def loop(i: Int): IO[Int] =
      if (i % depth == 0) IO(i + 1).flatMap(loop)
      else if (i < size) loop(i + 1).flatMap(i => IO(i))
      else IO(i)

    runCatsEffect3(IO(0).flatMap(loop))
  }
}
