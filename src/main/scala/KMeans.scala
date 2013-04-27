package lmad
import math._
import io.Source
import java.io.{File, FileWriter, PrintWriter}
import scala.util.Random
import scala.collection.parallel._
import scalaz.syntax.std.map._
import scalaz.syntax.id._
import scalaz.syntax.std.boolean._


class KMeans(k:Int, limit:Int=50) {

  type ProcState = (Int, Boolean, Array[Int])

  def mkCentroidArray(cluster:Int, fv:FeatVec):Array[FeatVec] = Array.tabulate(k) { 
    idx => if (idx == cluster) fv else Map.empty[String, Double]
  }
  def addFVArrays(fvl:Array[FeatVec], fvr:Array[FeatVec]):Array[FeatVec] = Array.tabulate(k) {
    idx => fvl(idx) + fvr(idx)
  }
  def computeCentroids(data:Array[FeatVec], clusters:Array[Int]):Array[FeatVec] = {
    val mkArr = (idx:Int) => mkCentroidArray(clusters(idx), data(idx))
    val centArrs = (0 until data.size par) map (mkArr) reduce (addFVArrays(_, _))
    centArrs map (fv => (1/data.size.toDouble) *: fv)
  }

  def updateClusters(data:Array[FeatVec], centroids:Array[FeatVec], clusters:Array[Int]):(Boolean,Array[Int]) = {
    val newClusters = data.par map { fv => (0 until k) maxBy {cl => fv cosine centroids(cl)}}
    val changed = (0 until data.size) exists {idx => clusters(idx) != newClusters(idx)}
    changed -> newClusters.seq.toArray
  }

  def step(data:Array[FeatVec])(state:ProcState):ProcState = state match { case (iter, _, clusters) => 
    val centroids = computeCentroids(data, clusters)
    val (changed, newClusters) = updateClusters(data, centroids, clusters)
    (iter + 1, changed, newClusters)
  }

  def init(data:Array[FeatVec]):ProcState = (0, true, Random.shuffle((0 until data.size).map(_ % k)).toArray)

  def test(state:ProcState):Boolean = state match { case (iter, changed, _) => changed && (iter < limit) }

  def cluster(data:Array[FeatVec]):ProcState = init(data) doWhile(step(data)(_), test(_))
}


object KMeansTester extends App {
  def loadData:(Array[FeatVec], Array[String]) = DataLoader.loadUserVecs.toSeq.unzip match {
    case (users, featVecs) => (featVecs.toArray, users.toArray)
  }

  def runTest(data:Array[FeatVec], users:Array[String])(k:Int):(Int, List[List[String]]) = {
    val (iter, _, clusters) = (new KMeans(k)).cluster(data)
    val clusteredUsers = ((clusters zip users).groupBy(_._1).mapValues(s => s.map(_._2).toList).values.toList)
    (iter + 1) -> clusteredUsers
  }

  def dissentersCount(dissenters:Set[String])(cluster:List[String]):Option[String] = {
    val count = cluster count (dissenters contains _)
    (count > 0) option (f"""cluster with $count%d has users ${cluster mkString(" ")}""")
  }

  def printClusteredUsersEval(clusteredUsers:List[List[String]]):Unit = {
    clusteredUsers flatMap (dissentersCount(loadDissenters)(_)) foreach(println(_))
  }

  val kMax = 12
  val (data, users) = loadData
  val test = runTest(data, users)(_)
  (2 to kMax) foreach { k =>
    println(f"running k-means with k=$k%d")
    val (iters, clusteredUsers) = test(k)
    println(f"k-means completed after $iters%d steps")
    printClusteredUsersEval(clusteredUsers)
  }
}
