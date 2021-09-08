package zio

import cats.effect.implicits._
import cats.effect.{IO => CIO}
import cats.implicits._
import org.openjdk.jmh.annotations._
import zio.BenchmarkUtil._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration => ScalaDuration}
import scala.concurrent.{Await, Future}

@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class CollectAllBenchmark {

  @Param(Array("10", "100"))
  var count: Int = 100

  val parallelism: Int = java.lang.Runtime.getRuntime.availableProcessors

  @Benchmark
  def catsCollectAll(): Long = {
    val tasks  = (0 until count).map(_ => CIO(1)).toList
    val result = tasks.sequence.map(_.sum.toLong)
    runCatsEffect3(result)
  }

  @Benchmark
  def catsCollectAllPar(): Long = {
    val tasks  = (0 until count).map(_ => CIO(1)).toList
    val result = tasks.parSequence.map(_.sum.toLong)
    runCatsEffect3(result)
  }

  @Benchmark
  def catsCollectAllParN(): Long = {
    val tasks  = (0 until count).map(_ => CIO(1)).toList
    val result = tasks.parSequenceN(parallelism).map(_.sum.toLong)
    runCatsEffect3(result)
  }

  @Benchmark
  def zioCollectAll(): Long = {
    val tasks  = (0 until count).map(_ => ZIO.succeed(1)).toList
    val result = ZIO.collectAll(tasks).map(_.sum.toLong)
    runZio(result)
  }

  @Benchmark
  def zioCollectAllPar(): Long = {
    val tasks  = (0 until count).map(_ => ZIO.succeed(1)).toList
    val result = ZIO.collectAllPar(tasks).map(_.sum.toLong)
    runZio(result)
  }

  @Benchmark
  def zioCollectAllParN(): Long = {
    val tasks  = (0 until count).map(_ => ZIO.succeed(1)).toList
    val result = ZIO.collectAllParN(parallelism)(tasks).map(_.sum.toLong)
    runZio(result)
  }

  @Benchmark
  def futureCollectAll(): Long = {
    val futures         = (0 until count).map(_ => Future(1)).toList
    val f: Future[Long] = Future.sequence(futures).map(_.sum.toLong)
    Await.result(f, ScalaDuration.Inf)
  }

}
