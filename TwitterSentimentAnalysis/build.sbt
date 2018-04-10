name := "Twitter"
version := "1.0"
scalaVersion := "2.11.0"
libraryDependencies ++= Seq(
"org.apache.spark" %% "spark-core" % "1.5.2",
"org.apache.spark" %% "spark-sql" % "1.5.2",
"org.apache.spark" %% "spark-streaming" % "1.5.2",
"org.apache.spark" %% "spark-streaming-twitter" % "1.5.2",
"org.apache.spark" %% "spark-mllib" % "1.5.2",
"joda-time" % "joda-time" % "2.1",
"org.joda" % "joda-convert" % "1.8.1")

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1"
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1" classifier "models"
