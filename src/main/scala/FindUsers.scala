package lmad;
import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.CSVWriter
import java.util.Date
//import java.text.DateFormat
import au.com.bytecode.opencsv.{CSVReader => JCSVReader, CSVWriter => JCSVWriter}
import java.io.{FileReader, FileWriter, File}

case class LMADownloadLine(id:Long, user:String, tweet:String, date:Date)

object DLsPath {
  val path = s"$basePath/dls/"
  val files = List("lma_4_09_4_10.csv", "lma_4_10_4_11.csv", "lma_4_11_4_12.csv", "lma_4_12_4_13.csv")
  val targets = files map (path + _)

  val cleanFile = "organized.csv"
  val cleanPath = path+cleanFile
}


case class DLsLoader(files:List[String],skipFirst:Boolean=false) {
  import DLsPath.path

  class Iterating(file:String) {
    val reader = if(skipFirst) {
      new JCSVReader(new FileReader(file), ',', '"', '\\', 1) 
    } else { 
      new JCSVReader(new FileReader(file), ',', '"', '\\')
    }
    def next():Option[Array[String]] = {
      Option(reader.readNext) match {
        case Some(stuff) => Some(stuff)
        case None => {reader.close(); None}
      }
    }
  }

  def streamer(file:String) = {
    val it = new Iterating(file)
    Stream.continually(it.next()).takeWhile(_.isDefined)
  }

  //val df = DateFormat.getDateTimeInstance(DateFormat.FULL)
  def loadFiles:Stream[LMADownloadLine] = for {
    file <- files.toStream
    lines <- streamer(file)
    line <- lines
  } yield line.toList match {
    case List(sId, user, tweet, date) => LMADownloadLine(sId.toLong, user, tweet, new Date(Date.parse(date)))
    case other => println(s"got this $other"); throw new Exception("wtf")
  }


  def getUnique:List[LMADownloadLine] = loadFiles.toList.map {
    lmadl => (lmadl.id, lmadl)
  }.toMap.values.toList

  def groupedUsers:Map[String, List[LMADownloadLine]] = getUnique groupBy (_.user)
}

case class DLsWriter(target:String) {
  def write(cleans:Seq[LMADownloadLine]):Unit = {
    val writer = new JCSVWriter(new FileWriter(target),',','"','\\')
    cleans foreach {
      case LMADownloadLine(id, user, tweet, date) => writer.writeNext(Array(id.toString, user, tweet, date.toString))
    }
    writer.close()
  }
}

object DLsCheck extends App {
  val uMap = DLsLoader(DLsPath.targets,true).groupedUsers
  val userCount = uMap.size
  val tweetCount = uMap.values.map(_.length).reduce(_+_) 
  println(f"have $userCount%d users and $tweetCount%d tweets")
}

object DLsClean extends App {
  val uMap = DLsLoader(DLsPath.targets,true).groupedUsers
  val cleans = uMap.values.reduce (_ ++ _)
  DLsWriter(DLsPath.cleanPath).write(cleans)
}

