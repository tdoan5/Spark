import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object SentimentAnalysisUtils {

  val nlpProps = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
    props
  }

  def detectSentiment(message: String): SENTIMENT_TYPE = {

    val pipeline = new StanfordCoreNLP(nlpProps)

    val annotation = pipeline.process(message)
    var sentiments: ListBuffer[Double] = ListBuffer()
    var sizes: ListBuffer[Int] = ListBuffer()

    var longest = 0
    var mainSentiment = 0
    
    // An Annotation is a Map and you can get and use the various analyses individually.
    // For instance, this gets the parse tree of the first sentence in the text.
    // Iterate through tweet
    for (tweetMsg <- annotation.get(classOf[CoreAnnotations.SentencesAnnotation])) {
      // Create a RNN parse tree
      val parseTree = tweetMsg.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])
      // Detect Sentiment
      val tweetSentiment = RNNCoreAnnotations.getPredictedClass(parseTree)
      val partText = tweetMsg.toString

      if (partText.length() > longest) {
        mainSentiment = tweetSentiment
        longest = partText.length()
      }

      sentiments += tweetSentiment.toDouble
      sizes += partText.length
    }

    val weightedSentiments = (sentiments, sizes).zipped.map((sentiment, size) => sentiment * size)
    var weightedSentiment = weightedSentiments.sum / (sizes.fold(0)(_ + _))

    if (weightedSentiment <= 0.0)
      "NOT_UNDERSTOOD"
    else if (weightedSentiment < 1.6)
      "NEGATIVE"
    else if (weightedSentiment <= 2.0)
      "NEUTRAL"
    else if (weightedSentiment < 5.0)
      "POSITIVE"
    else "NOT_UNDERSTOOD"    
  }
}
