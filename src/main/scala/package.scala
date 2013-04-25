package object lmad {
  import scalaz.syntax.std.map._

  type FeatVec = Map[String, Double]

  class FeatVecOps(val fv:FeatVec) {
    def +(other:FeatVec):FeatVec = {
      fv.unionWith(other)(_+_)
    }
  }
  implicit def fv2Ops(fv:FeatVec) = new FeatVecOps(fv)

}
