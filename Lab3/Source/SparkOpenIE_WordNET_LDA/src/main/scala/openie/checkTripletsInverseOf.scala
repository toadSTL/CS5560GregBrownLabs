package openie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import java.io._
import scala.io.Source
import scala.collection.mutable.ListBuffer
import java.lang.String._

import org.apache.spark.{SparkConf, SparkContext}

object checkTripletsInverseOf {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir", "D:\\winutils")

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    val triplets = sc.textFile("ICP11/Triplets");

    val temp = triplets.map(s => {
      val arr = s.split(",")
      (arr(0), arr(1), arr(2))
    }).collect.toSet

    var results:String = ""

    val temp2 = triplets.map(s => {
      val arr = s.split(",")
      (arr(0),arr(1),arr(2))
    }).map(a =>{
      temp.foreach(arr =>{
        var sub1 = a._1
        var sub2 = arr._1
        var pred1 = a._2
        var pred2 = arr._2
        var obj1 = a._3
        var obj2 = arr._3
        if(sub1==obj2&&sub2==obj1&&pred1!=pred2){
          println( (pred1, pred2) )
          results+=pred1+","+pred2+"\n"
        }
      })
    })

    val pwdummy = new PrintWriter(new File("output\\dumb"))
    pwdummy.write(temp2.collect().mkString("\n"))
    pwdummy.close

    val pw = new PrintWriter(new File("output\\InverseCandidates.txt"))
    pw.write(results)
    pw.close

  }

}
