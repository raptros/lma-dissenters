package lmad;
import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.CSVWriter
import java.util.Date
import java.util.GregorianCalendar
import java.text.DateFormat
import java.io.File
import collection.JavaConversions._

case class Count(inDateTarget:Int, hasLMATag:Int, both:Int, total:Int) {
  def +(other:Count):Count = Count(
    inDateTarget + other.inDateTarget, 
    hasLMATag + other.hasLMATag, both + other.both, total + other.total)
}

object CountStuff {
  implicit def boolean2Int(bool:Boolean):Int = if (bool) 1 else 0
  def hasLMATag(ldl:LMADownloadLine):Boolean = {
    ldl.tweet.toLowerCase.contains("#lma13")
  }
  //LMA13 was 4/8 to 4/10, but we'll do a couple days around that,
  //because people were still talking about it.
  val lmaStart = (new Date(113, 3, 7))
  val lmaEnd = (new Date(113, 3, 12, 23, 59))

  def inDateTarget(ldl:LMADownloadLine):Boolean = {
    ldl.date.after(lmaStart) && ldl.date.before(lmaEnd)
  }

  def count(ldl:LMADownloadLine) = {
    val hasLMA = hasLMATag(ldl)
    val inDate = inDateTarget(ldl)
    Count(inDate, hasLMA, inDate && hasLMA, 1)
  }
}

object LMADownloads {
  val lmaDir = "/mnt/stuff/data/lmasearch/lmaers/"
  val corpusDir = "/mnt/stuff/data/lmasearch/lmaSelected/"
  lazy val lmaerTargets = Option((new File(lmaDir).listFiles)).map(_.toList).getOrElse(Nil).map(_.getAbsolutePath)
  
  def printCount(pair:(String, Count)) = pair match {
    case (user, Count(inDateTarget, hasLMATag, both, total)) => println(s"$inDateTarget\t$hasLMATag\t$both\t${inDateTarget+hasLMATag-both}\t$total\t$user")
  }

  def printCountMap(countMap:Map[String, Count]):Unit = {
    countMap foreach (printCount(_))
    printCount(s"all(${countMap.keys.size})" -> countMap.values.reduce(_+_))
  }
}

object Stats extends App {
  val uMap = DLsLoader(LMADownloads.lmaerTargets).groupedUsers
  val countMap = uMap.mapValues { 
    tweets => tweets map (CountStuff.count(_)) reduce (_+_)
  }
  LMADownloads.printCountMap(countMap)
}

object SelectTweets extends App {
  val uMap = DLsLoader(LMADownloads.lmaerTargets).groupedUsers
  val countMap = uMap.mapValues { 
    tweets => tweets map (CountStuff.count(_)) reduce (_+_)
  }
  countMap filter { 
    case (user, Count(inDateTarget, hasLMATag, both, total)) => (inDateTarget >= 5 && hasLMATag >= 5)
  } map {
    case (user, Count(inDateTarget, hasLMATag, both, total)) => (user -> uMap(user).filter(CountStuff.hasLMATag(_)))
  } foreach {
    case (user, tweets) => DLsWriter(s"${LMADownloads.corpusDir}${user}.csv").write(tweets)
  }
}
