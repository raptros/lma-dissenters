package lmad;
import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.CSVWriter
import scala.io.Source

import twitter4j._
import collection.JavaConversions._

import java.lang.Thread

class TweetDownloader(val user:String, val path:String) {
  val twitter = TwitterFactory.getSingleton()
  //number of minutes in req period times number of seconds in minute
  //over number of requests in period  all times number of milliseconds in second
  //i.e. 5 seconds or 5000 milliseconds
  val sleeptime = ((15*60)/180)*1000

  def mkPaging(maxId:Option[Long]):Paging = maxId
  .map (mid => new Paging(1, 200, 1, mid))
  .getOrElse (new Paging(1, 200))

  /** Calls out to twitter to download a bunch of tweets; returns them along with the oldest ID among them.
    * Returns None if something goes wrong.
    */
  def download(maxId:Option[Long]):Option[(Long, List[Status])] = try {
    Thread.sleep(sleeptime)
    val tweets = twitter.getUserTimeline(user, mkPaging(maxId))
    val oMin = if (tweets.isEmpty) None else Some(tweets.minBy(_.getId).getId)
    oMin map (_ -> tweets.toList)
  } catch {
    case (te:TwitterException) => {println(s"failed: ${te.getErrorMessage}"); None}
  }

  class StrangeIterator {
    var maxId:Option[Long] = None
    def next:Option[(Long, List[Status])] = download(maxId) match {
      case Some(Pair(newMax, items)) => {maxId = Some(newMax-1); Some(newMax -> items)}
      case None => None
    }
  }

  def downloadAllPossible:Stream[Status] = {
    val strange = new StrangeIterator
    Stream.continually(strange.next).takeWhile(_.isDefined).map(_.get).flatMap {
      case (id, items) => items
    }
  }

  def saveTweets:Unit = {
    println(s"saving as many tweets as possible for $user")
    val statuses = downloadAllPossible map { 
      status => LMADownloadLine(status.getId, user, status.getText, status.getCreatedAt)
    }
    DLsWriter(path).write(statuses)
  }
}

object DownloadLMAers extends App {
  val basedir = s"$basePath/lmaers/"
  val uMap = DLsLoader(List(DLsPath.cleanPath),true).groupedUsers
  uMap.keys map {
    user => new TweetDownloader(user, s"$basedir$user.csv")
  } foreach (_.saveTweets)

}

object Redownload extends App {
  val basedir = s"$basePath/lmaers/"
  val uMap = DLsLoader(List(DLsPath.cleanPath),true).groupedUsers
  val redownload = Source.fromFile(s"$basePath/redownload").getLines.toList
  redownload map {
    user => new TweetDownloader(user, s"$basedir$user.csv")
  } foreach (_.saveTweets)
}

