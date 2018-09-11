package openie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import java.io._
/**
  * Created by Mayanka on 27-Jun-16.
  */
object SparkOpenIE {

  def main(args: Array[String]) {
    // Configuration
    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    // For Windows Users
    System.setProperty("hadoop.home.dir", "D:\\winutils")


    // Turn off Info Logger for Console
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    var i = 0
    val numSplits = new Array[Int](10)
      for (i <- 1 to 10)
      {
        numSplits(i-1) = 0;
        val input = sc.textFile("data\\"+i+".txt").map(line => {
          //Getting OpenIE Form of the word using lda.CoreNLP
          val t = CoreNLP.returnTriplets(line)
          val temp = t.split('(')
          var test = (numSplits(i-1)+(temp.length-1))
          numSplits(i-1) = test
          System.out.println("NUM TRIPLETS: \t" + temp.length);
          System.out.println("test" + i +": \t" + test);
          System.out.println("Num Splits " + i + " :\t" + numSplits(i-1));
          t
        })


        val pw = new PrintWriter(new File("data\\triplet" + i + ".txt"))
        pw.write(input.collect().mkString("\n\n***\n\n"))
        pw.write("\nNumber of triplets " + i + " :\t" + numSplits(i-1))
        pw.close
      }
    //input.foreach(println)
    //println(input.collect().mkString("\n"))
    //open the abstract files, and iterate over them writing into a new file the results.
    //println(CoreNLP.returnTriplets("The dog has a lifespan of upto 10-12 years."))
    //println()



  }

}
