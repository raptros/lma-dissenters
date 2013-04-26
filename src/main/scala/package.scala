package object lmad {
  import scalaz.syntax.std.map._
  import io.Source

  type FeatVec = Map[String, Double]
  type DataSet = Map[String, Seq[FeatVec]]
  type UserSet = Map[String, FeatVec]

  type TripleDouble = Triple[Double, Double, Double]
  class TripleDoubleOps(triple:TripleDouble) {
    def +(triple2:TripleDouble) = (triple._1 + triple2._1, triple._2 + triple2._2, triple._3 + triple2._3)
  }
  implicit def triple2TDO(triple:TripleDouble) = new TripleDoubleOps(triple)

  class FeatVecOps(val fv:FeatVec) {
    import math._
    def +(other:FeatVec):FeatVec = {
      fv.unionWith(other)(_+_)
    }
    def *:(scale:Double):FeatVec = fv.mapValues(_*scale)
    def :*(scale:Double):FeatVec = fv.mapValues(_*scale)

    def vecLength:Double = sqrt(fv.values map (v => pow(v, 2)) reduce(_+_))

    def norm2alize:FeatVec = :*(1/vecLength)

    def <*>(other:FeatVec):FeatVec = {
      fv.intersectWith(other)(_*_)
    }
    def dot(other:FeatVec):Double = {
      <*>(other).values reduce(_+_)
    }

    def cosine(other:FeatVec):Double = {
      fv2Ops(norm2alize) dot fv2Ops(other).norm2alize
    }
  }
  implicit def fv2Ops(fv:FeatVec) = new FeatVecOps(fv)

  val basePath = "/mnt/stuff/data/lmasearch"
  val dissentersPath = s"$basePath/dissenters.txt"
  def loadDissenters():Set[String] = Source.fromFile(dissentersPath).getLines.toSet
}
