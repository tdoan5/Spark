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
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale


import org.apache.spark.storage.StorageLevel
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File

import scala.util.Try

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
    
    //val filters = Array("dogs", "cats")
   
    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generat OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val sparkConf = new SparkConf().setAppName("SentimentAnalysis").setMaster("local[*]")
    // val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    val tweets = TwitterUtils.createStream(ssc, None, filters)

     tweets.print()

     /* Extract required columns from json object and also generate sentiment score for each tweet.
      * RDD can be saved into elasticsearch as long as the content can be translated to a document.
      * So each RDD should be transformed to a Map before storing in elasticsearch index twitter_082717/tweet.
      */
     tweets.foreachRDD{(rdd, time) =>
       rdd.map(t => {
         Map(
           "user"-> t.getUser.getScreenName,
           "created_at" -> t.getCreatedAt.getTime.toString,
           "location" -> Option(t.getGeoLocation).map(geo => { s"${geo.getLatitude},${geo.getLongitude}" }),
           "text" -> t.getText,
           "hashtags" -> t.getHashtagEntities.map(_.getText),
           "retweet" -> t.getRetweetCount,
           "language" -> t.getLang.toString(),
           "sentiment" -> SentimentAnalysisUtils.detectSentiment(t.getText).toString
         )
       }).saveToEs("twitter_041718/tweet")
     }
     

     ssc.start()
     ssc.awaitTermination()


  }

}

