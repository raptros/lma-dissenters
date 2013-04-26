package lmad;
import math._
import io.Source
import java.io.{File, FileWriter, PrintWriter}
import scala.util.Random

trait ClassifierTrainer {
  def train(users:UserSet, classMap:Map[String, String]):ClassifierModel
}

trait ClassifierModel {
  def test(fv:FeatVec):String  
}


class NBayesModel(val classes:Array[String],val priors:Array[Double], val means:Array[FeatVec], val variances:Array[FeatVec]) extends ClassifierModel {
  def computeLogProbDist(v:Double, mean:Double, svar:Double):Double = {
    val fst = -log(sqrt(2*Pi*svar))
    val snd = -pow(v-mean, 2)/(2*svar)
    fst+snd
  }
  def getLogProb(fv:FeatVec, cPrior:Double, cMeans:FeatVec, cVars:FeatVec):Double = {
    /*var fLogs = for {
      (f, v) <- fv
      mean <- cMeans get f
      svar <- cVars get f
    } yield computeLogProbDist(v, mean, svar)*/
    
    var fLogs = fv.keys map { f =>
      val v = fv(f)
      val p = for {
        mean <- cMeans get f
        svar <- cVars get f
      } yield computeLogProbDist(v, mean, svar)
      p getOrElse(-5000d)
    }
    fLogs.foldLeft(log(cPrior))(_+_)
  }
  def test(fv:FeatVec):String = {
    val maxIdx = (0 until classes.length) maxBy {
      i => getLogProb(fv, priors(i), means(i), variances(i))
    }
    classes(maxIdx)
  }
}

object NBayesTrainer extends ClassifierTrainer {
  def calcVariances(cMeans:FeatVec, cCount:Int, cData:Seq[FeatVec]):FeatVec = {
    val cSumSquareVariations:FeatVec = cData map { 
      fv => fv.map { case (f, v) => f -> pow(v - cMeans(f), 2) }
    } reduce (_+_)
    (1/cCount.toDouble) *: cSumSquareVariations
  }
  def train(users:UserSet, classMap:Map[String, String]):NBayesModel = {
    val data = users groupBy {
      case (k,fv) => classMap(k)
    } mapValues (_.values.toSeq)
    val classes = data.keys.toArray
    //val counts:Seq[Int] = classes map (c => data(c).size)
    val countMap:Map[String, Int] = data mapValues (_.size)
    val counts = classes map (c => countMap(c))
    val total:Int = counts reduce (_+_)
    val priors:Array[Double] = counts map (c => c.toDouble/total) toArray
    val classMeans:Map[String, FeatVec] = data mapValues (cData => cData reduce (_+_)) map {
      case (cName, cTotal) => cName -> (1/countMap(cName).toDouble) *: cTotal
    }
    val means = classes map (c => classMeans(c)) toArray
    val classVars:Map[String, FeatVec] = data map {
      case (cName, cData) => cName -> calcVariances(classMeans(cName), countMap(cName), cData)
    }
    val variances:Array[FeatVec] = classes.map (c => classVars(c)).toArray
    new NBayesModel(classes, priors, means, variances)
  }
}

class Evaluator(model:ClassifierModel, testUsers:UserSet, classMap:Map[String, String]) {
  val pos = "dissenters"
  val neg = "supporters"
  type ConfMat = (Int, Int, Int, Int) //tp, fp, fn, tn
  val unitConfs = Map(
    (pos, pos) -> (1, 0, 0, 0),
    (neg, pos) -> (0, 1, 0, 0),
    (pos, neg) -> (0, 0, 1, 0),
    (neg, neg) -> (0, 0, 0, 1)
  )
  def addconfs(c1:ConfMat, c2:ConfMat):ConfMat = (c1, c2) match {
    case ((tp1,fp1,fn1,tn1),(tp2,fp2,fn2,tn2)) => (tp1+tp2, fp1+fp2, fn1+fn2, tn1+tn2)
  }
  def runEval():ConfMat = {
    val confs = for {
      (u, fv) <- testUsers
      c <- classMap get u
      p = model.test(fv)
      conf <- unitConfs get (c->p)
    } yield conf
    confs reduce(addconfs(_,_))
  }
  lazy val (tp, fp, fn, tn) = runEval()
  lazy val precision = tp.toDouble / (tp + fp)
  lazy val recall = tp.toDouble / (tp + fn)
  lazy val accuracy = (tp + tn).toDouble / (tp + fp + fn + tn)
}


object ModelData extends App {
  val vectorsPath = s"$basePath/vectors0/"
  def loadVector(user:String):FeatVec = {
    Source.fromFile(s"$vectorsPath$user.vec").getLines.map(l => l.split('\t') match {case Array(f, v) => f -> v.toDouble}).toMap
  }
  def splitUsers(userSet:UserSet):Array[UserSet] = {
    val arr = Array.fill[UserSet](10)(Map.empty[String,FeatVec])
    Random.shuffle(userSet.toSeq).zipWithIndex foreach {
      case ((k,v), i) => arr(i % 10) = arr(i%10) + (k->v)
    }
    arr
  }
  def runSplit(splits:Array[UserSet], classMap:Map[String, String])(idx:Int):(Double, Double, Double) = {
    val training = (0 until 10).filterNot(_==idx).map(splits(_)).reduce(_++_)
    val model = NBayesTrainer.train(training, classMap)
    val eval = new Evaluator(model, splits(idx), classMap)
    (eval.precision, eval.recall, eval.accuracy)
  }
  lazy val users = Option((new File(vectorsPath).listFiles)).map(_.toList).getOrElse(Nil).map(s => s.getName.split('.').apply(0))
  lazy val userVecs = users map { u => u -> loadVector(u)} toMap
  lazy val dissenters = loadDissenters()
  lazy val classMap = userVecs.keys map { u => u -> (if (dissenters contains u) "dissenters" else "supporters")} toMap
  //ok now split up the users into 10 sets, each of size 12, then do 10-fold cross-val
  lazy val splits = splitUsers(userVecs)
  splits foreach {s => println(s.keys)}
  lazy val run = runSplit(splits, classMap)(_)
  println("running")
  println("fold\tprec\trec\tacc")
  val results:Seq[TripleDouble] = (0 until 10) map { idx =>
    val (prec, rec, acc) = run(idx)
    println(f"$idx%d\t$prec%f\t$rec%f\t$acc%f")
    (prec, rec, acc)
  } 
  results reduce(_ + _) match {
    case (prec, rec, acc) => {
      val (p, r, a) = (prec/10, rec/10, acc/10)
      println(f"avg\t$p%f\t$r%f\t$a%f")
    }
  }
}
