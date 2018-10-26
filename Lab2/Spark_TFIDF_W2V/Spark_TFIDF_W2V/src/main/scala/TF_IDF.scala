import java.io.{File, PrintWriter}

import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.HashMap

//Stopwords from https://www.ranks.nl/stopwords

/**
  * Updated by Greg on 30-09-2018
  * Created by Mayanka on 19-06-2017.
  */
object TF_IDF{
  def main(args: Array[String]): Unit = {

    System.setProperty("hadoop.home.dir", "D:\\winutils")

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    //Reading the Text File
    val documents = sc.wholeTextFiles("abstracts") //WholeTextFiles


    val stopword = sc.textFile("data/stopwords.txt")

    val sw = stopword.flatMap(x => x.split(",")).map(_.trim)

    val broadcastSW = sc.broadcast(sw.collect.toSet)

    //RDD of sequence of string
    //Getting individual words from corpus and removing stopwords
    val documentseq = documents.map(f => {
      val splitString = f._2.split(" ").filter(!broadcastSW.value.contains(_))
      val arr = splitString.map(w => {
        //println(w)
        var tw = ""
        if(w.takeRight(1)==","||w.takeRight(1)==":"||w.takeRight(1)=="."){
          tw = w.dropRight(1)
        }else{
          tw = w
        }
        tw.replaceAll("\\(", "").replaceAll("\\)", "")
      })
      /*
      splitString.foreach(w => {
        println(w)
        if(w.takeRight(1)==","){
          w.substring(0, w.length - 1)
        }
      })
       */

      //splitString.foreach(_.replaceAll(",",""))
      arr.toSeq
    })
    //Creating an object of HashingTF Class
    val hashingTF = new HashingTF()
    //Creating Term Frequency of the document
/*
val temp = documentseq.map(arr => {
      arr.foreach(word => {
        println(word)
        println(word.takeRight(1))
        if(word.takeRight(1)==",") {
          println("here")
          word.substring(0, word.length - 1)
          println(word)
        }
      })
      arr
    })
 */

    /*
    documentseq.foreach(_.foreach(w =>{
      w.replaceAll(",","")
    }))
    */
    val tf = hashingTF.transform(documentseq)
    tf.cache()
    val idf = new IDF().fit(tf)
    //Creating Inverse Document Frequency
    val tfidf = idf.transform(tf)

    val tfidfvalues = tfidf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      //println(ff(2))
      val values = ff(2).replace("]", "").replace(")", "").split(",")
      values
    })
    val tfidfindex = tfidf.flatMap(f => {
      val ff: Array[String] = f.toString.replace(",[", ";").split(";")
      //println(ff(1))
      val indices = ff(1).replace("]", "").replace(")", "").split(",")
      indices
    })
    val tfidfData = tfidfindex.zip(tfidfvalues)

    var hm = new HashMap[String, Double]

    tfidfData.collect().foreach(f => {
      hm += f._1 -> f._2.toDouble
    })
    val mapp = sc.broadcast(hm)
    val documentData = documentseq.flatMap(_.toList)
    val dd = documentData.map(f => {
      val i = hashingTF.indexOf(f)
      val h = mapp.value
      (f, h(i.toString))
    })

    var s:String=""

    val dd1 = dd.distinct().sortBy(_._2, false)
    dd1.filter(w => {
      w._2>0&&w._2<3
    }).take(800).foreach(f => {
      println(f)
      s+=f+"\n"
    })

    /*
    dd1.take(200).foreach(f => {
      println(f)
      s+=f+"\n"
    })
     */

    //Output the data to text files
    val pw = new PrintWriter(new File("update/TF_IDF_Out.txt"))
    pw.write(s)
    pw.close()

  }

}
