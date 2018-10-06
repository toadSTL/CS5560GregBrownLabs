import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}
import java.net.{HttpURLConnection, URL, URLEncoder}
import java.util.Properties

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, PartOfSpeechAnnotation, SentencesAnnotation, TokensAnnotation}
import org.apache.spark.{SparkConf, SparkContext}
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.spark.mllib.feature.{HashingTF, IDF, Word2Vec}

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.io.Source
import scala.collection.mutable.ListBuffer


object ontTest {

  private val REST_URL = "https://data.bioontology.org"
  private val API_KEY = "ec511abb-8761-41a6-a094-e6f931afa672"
  private val mapper: ObjectMapper = new ObjectMapper


  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir","D:\\winutils")

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    val in = sc.textFile("data/justMedWords.txt")

    val medWords = in.flatMap(a =>{
      val r = a.split("\n")
      r
    })

    val medString = medWords.collect().distinct.mkString(" ") // string for ontologies

    val textToAnnotate = URLEncoder.encode(medString, "ISO-8859-1")

    // Get just annotations of data for ontologies
    val urlParameters = "/annotator?text=" + textToAnnotate
    val annotations: JsonNode = jsonToNode(getOntologies(REST_URL + urlParameters))

    println(REST_URL + urlParameters)

    // get ontologies
    val ontData = annotations.map(annotation => {
      println(annotation.get("annotatedClass").get("links").get("self").asText)
      getOntologyData(annotation.get("annotatedClass").get("links").get("self").asText)
    })

    // what is the error i'm getting in mine about 400

    val ontWords = ontData.flatMap(list => list)
    val ontCount = ontWords.toList.map(list => {
      println(list._1)
      (list._1, 1)
    })
    val ontologies = ontWords.toList.map(list => {
      println(list._1+", "+list._2)
      (list._1, list._2)
    }).distinct
    sc.parallelize(ontologies)//.saveAsTextFile("/test/OntOut.txt")

    var s1:String=""
    val testOnt = ontologies.map(a =>{
      println("("+a._1+", "+a._2+")\n")
      s1+="("+a._1+", "+a._2+")\n"
      a
    })

    val pw1 = new PrintWriter(new File("output/Ont_Data.txt"))
    pw1.write(s1)
    pw1.close()

    val outOnt = sc.parallelize(ontCount).reduceByKey(_+_)
    //outOnt.saveAsTextFile("/test/OntCount.txt")

    val pw2 = new PrintWriter(new File("output/Ont_Count.txt"))
    outOnt.collect().foreach(a => pw2.write(a._1 + ", " + a._2 + "\n"))
    pw2.close()
  }

  def get(url: String) = scala.io.Source.fromURL(url).getLines()

  // changes string to JsonNode
  def jsonToNode (json: String): JsonNode =
  {
    var root: JsonNode = null
    try
      root = mapper.readTree(json)
    catch {
      case e: Exception =>
        None
    }
    root
  }


  def getOntologies(urlIn: String): String = {
    var line: String = null
    var result: String = ""

    try {
      val url = new URL(urlIn)
      val conn = url.openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestMethod("GET")
      conn.setRequestProperty("Authorization", "apikey token=" + API_KEY)
      conn.setRequestProperty("Accept", "application/json")

      val rd = new BufferedReader(new InputStreamReader(conn.getInputStream))
      while({line = rd.readLine; line != null})
        result += line
      rd.close()
    } catch {
      case e: Exception =>
        None
    } // end try/catch

    result
  } // end getOntologies

  // parses the data for the individual ontologies
  def getOntologyData(urlIn: String): ListBuffer[(String, String)] = {
    val ontWords = ListBuffer.empty[(String, String)]
    var line: String = null
    var result: String = ""

    try {
      val url = new URL(urlIn)
      val conn = url.openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestMethod("GET")
      conn.setRequestProperty("Authorization", "apikey token=" + API_KEY)
      conn.setRequestProperty("Accept", "application/json")

      val rd = new BufferedReader(new InputStreamReader(conn.getInputStream))
      while({line = rd.readLine; line != null})
        result += line
      rd.close()

      val classDetails: JsonNode = jsonToNode(result)

      if(classDetails != null)
      {
        ontWords += ((classDetails.get("prefLabel").asText.toLowerCase, classDetails.get("links").get("ontology").asText))
      } // end if
    } catch {
      case e: Exception =>
        None
    } // end try/catch
    ontWords
  }

}
