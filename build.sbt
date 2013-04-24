name := "lma-dissenters"

version := "0.1"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "apache" at "https://repository.apache.org/content/repositories/releases",
  "gwtwiki" at "http://gwtwiki.googlecode.com/svn/maven-repository/"
)

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.0"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "3.0.3"

libraryDependencies += "net.sf.opencsv" % "opencsv" % "2.3"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "0.7.0"

scalacOptions ++= Seq("-deprecation", "-unchecked")

