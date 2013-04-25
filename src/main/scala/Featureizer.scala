package lmad;
import java.util.Date
import collection.JavaConversions._
import java.io.{File, FileWriter, PrintWriter}


object Featurizer extends App {
  type MkFeat = Function1[LMADownloadLine, FeatVec]
  val featMakers:List[MkFeat] = List(words(_), moddedRTs(_), mentions(_), hashtags(_), containsURL(_))
  val vectorsPath = "/mnt/stuff/data/lmasearch/vectors0/"

  def words(tweet:LMADownloadLine):FeatVec = tweet.tweet.split(" ")
  .withFilter(!_.startsWith("@")).withFilter(!_.startsWith("#")).withFilter(_!="RT").withFilter(!_.startsWith("http"))
  .map(_.trim.replaceAll("\\W", "")).withFilter(!_.isEmpty)
  .map {
    word => Map(word.toLowerCase -> 1.0)
  }.foldLeft(Map.empty[String,Double])(_+_)

  def moddedRTs(tweet:LMADownloadLine):FeatVec = {
    val v = if (tweet.tweet.contains("RT") && tweet.tweet.split("RT")(0).size > 0) 1.0 else 0.0
    Map("_moddedRTs" -> v)
  }
  
  def hashtags(tweet:LMADownloadLine):FeatVec = {
    val hashtags = tweet.tweet.split(" ").filter(_.startsWith("#")).map("#" + _.trim.replaceAll("\\W",""))
    val count:FeatVec = Map("_hastagCount" -> hashtags.size.toDouble)
    hashtags.map(h => Map(h -> 1.0)).foldLeft(count)(_+_)
  }
  def mentions(tweet:LMADownloadLine):FeatVec = {
    val mentions = tweet.tweet.split(" ").filter(_.startsWith("@")).map("@" + _.trim.replaceAll("\\W",""))
    val count:FeatVec = Map("_mentionsCount" -> mentions.size.toDouble)
    mentions.map(m => Map(m -> 1.0)).foldLeft(count)(_+_)
  }
  
  def containsURL(tweet:LMADownloadLine):FeatVec = if (tweet.tweet.contains("http://"))
    Map("_containsURL" -> 1.0)
  else Map.empty[String, Double]

  def makeVector(user:String, tweets:List[LMADownloadLine]):FeatVec = {
    val vecs:Seq[FeatVec] = for(f <- featMakers; tweet <- tweets.par) yield f apply tweet
    vecs.reduce(_+_)
  }

  def writeVector(user:String, vector:FeatVec):Unit = {
    val writer = new PrintWriter(new FileWriter(s"$vectorsPath$user.vec"))
    vector foreach {
      case (d, v) => writer.println(f"$d%s\t$v%f")
    }
    writer.close()
  }

  import LMADownloads.corpusDir
  lazy val users = Option((new File(corpusDir).listFiles)).map(_.toList).getOrElse(Nil).map(s => s.getName.split('.').apply(0))
  lazy val targetted = users map (u => u -> (corpusDir + u + ".csv"))
  lazy val vectors:Seq[(String,FeatVec)] = targetted map {
    case (u, t) => (u -> makeVector(u, DLsLoader(List(t)).loadFiles.toList))
  }
  val writeVec = (writeVector(_:String,_:FeatVec)).tupled
  vectors foreach (writeVec(_))

}
