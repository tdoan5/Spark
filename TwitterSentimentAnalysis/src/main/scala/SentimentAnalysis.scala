import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.twitter._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import org.apache.spark.sql


import org.apache.spark.storage.StorageLevel
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File


object SentimentAnalysis {
 
  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: SentimentAnalysis <consumer key> <consumer secret> " +
        "<access token> <access token secret> [<filters>]")
      System.exit(1)
    }

    StreamingExamples.setStreamingLogLevels()

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)
    val filters = args.takeRight(args.length - 4)
    
    val keyword = filters(0)
   
    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generat OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("Twitter").setMaster("local[2]")
    // val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    val stream = TwitterUtils.createStream(ssc, None, filters)

  val tags = stream.flatMap { status => status.getHashtagEntities.map(_.getText)}

tags.countByValue()
   .foreachRDD { rdd =>
       val now = org.joda.time.DateTime.now()
       rdd
         .sortBy(_._2)
         .map(x => (x, now))
         .saveAsTextFile(s"./twitter/$now")
     }

val tweets = stream.filter {t =>
     val tags = t.getText.split(" ").filter(_.startsWith(keyword)).map(_.toLowerCase)
     tags.exists { x => true }
}
   

val data = tweets.map { status =>
   val sentiment = SentimentAnalysisUtils.detectSentiment(status.getText)
  
   val tagss = status.getHashtagEntities.map(_.getText.toLowerCase)

  (status.getText, sentiment.toString, tagss.toString())
   
    
}    
data.print()
data.saveAsTextFiles("./saveddata/") 

ssc.start()
    ssc.awaitTermination()

  }

}

