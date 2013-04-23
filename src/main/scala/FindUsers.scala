package lmad;
import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.CSVWriter

case class LMADownloadLine(id:Long, user:String, tweet:String, date:String)

object DLsPath {
  val path = "/mnt/stuff/data/lmasearch/dls/"
  val files = List("lma_4_09_4_10.csv", "lma_4_10_4_11.csv", "lma_4_11_4_12.csv", "lma_4_12_4_13.csv")

  val cleanFile = "organized.csv"
}

case class DLsLoader(files:List[String]) {
  import DLsPath.path
  def loadFiles:Stream[LMADownloadLine] = for {
    f <- files.toStream
    line <- CSVReader.open(path + f).toStream.tail
    Array(sId, user, tweet, date) = line.toArray
  } yield LMADownloadLine(sId.toLong, user, tweet, date)


  def getUnique:List[LMADownloadLine] = loadFiles.toList.map {
    lmadl => (lmadl.id, lmadl)
  }.toMap.values.toList

  def groupedUsers:Map[String, List[LMADownloadLine]] = getUnique groupBy (_.user)
}

case class DLsWriter(target:String) {
  import DLsPath.path
  def write(cleans:List[LMADownloadLine]):Unit =  CSVWriter.open(path + target) { writer =>
    cleans foreach {
      case LMADownloadLine(id, user, tweet, date) => writer.writeRow(List(id.toString, user, tweet, date))
    }
  }
}

object DLsCheck extends App {
  val uMap = DLsLoader(DLsPath.files).groupedUsers
  val userCount = uMap.size
  val tweetCount = uMap.values.map(_.length).reduce(_+_) 
  println(f"have $userCount%d users and $tweetCount%d tweets")
}

object DLsClean extends App {
  val uMap = DLsLoader(DLsPath.files).groupedUsers
  val cleans = uMap.values.reduce (_ ++ _)
  DLsWriter(DLsPath.cleanFile).write(cleans)
}

