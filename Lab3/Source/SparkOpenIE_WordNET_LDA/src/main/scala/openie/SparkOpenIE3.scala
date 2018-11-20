package openie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import java.io._
import scala.io.Source
import scala.collection.mutable.ListBuffer
/**
  * Created by Mayanka on 27-Jun-16.
  */
object SparkOpenIE3 {

  def main(args: Array[String]) {
    // Configuration
    System.setProperty("hadoop.home.dir", "D:\\winutils")

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    // Turn off Info Logger for Console
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val TF_IDF_Syn = sc.textFile("output/synTFIDF.txt")
    val TF_IDF_SynArr = TF_IDF_Syn.map(line =>{
      line.split(",")
    })

    TF_IDF_SynArr.foreach(a =>{
      a.foreach(println)
    })

    val classes = TF_IDF_SynArr.map(a => a(0).capitalize).collect.mkString("\n")

    val pwClasses = new PrintWriter(new File("output/classes.txt"))
    pwClasses.print("Chemical\nDisease\nGene\nMutation\nOther\nSpecies\n"+classes)
    pwClasses.close

    val pwFinalClasses = new PrintWriter(new File("finalOutput\\Classes"))
    pwFinalClasses.print("Chemical\nDisease\nGene\nMutation\nOther\nSpecies\n"+classes)
    pwFinalClasses.close

    val TFIDF_SynArr = TF_IDF_SynArr.collect


    val input = sc.wholeTextFiles("data")

//    val sentances = input.flatMap(doc => {
//      val sen = doc._2.split("\\. ")
//      sen
//    })


    //Replace ≥ with 'lessThanOrEqualTo
    //replace Γ with Gamma
    //replace Α and α with (alpha?) =|= A
    //replace χ with chi
    //replace ĸ with Kra
    //replace ‑ with - (they are different)
    val triplets = input.flatMap(abs => {
      val t = CoreNLP.returnTriplets(abs._2)
      println(t)
      val a = t.split("\\), \\(|" +                 //"), ("
        "\\)\\]\\[\\(|" +                           //")][("
        "\\)\\]\\[\\]\\[\\(|" +                     //")][][("
        "\\)\\]\\[\\]\\[\\]\\[\\(|" +               //")][][][("
        "\\)\\]\\[\\]\\[\\]\\[\\]\\[\\(|" +         //")][][][][("
        "\\)\\]\\[\\]\\[\\]\\[\\]\\[\\]\\[\\(")     //")][][][][][("

      val ret = a.map(t=>{
        //println(t)
        var arr = abs._1.split("/")
        //println(t+","+arr(arr.length-1))
        t+","+arr(arr.length-1)
      })
      ret
    }).map(trip => {
      //println(trip)
      val c = trip.replaceAll("\\)\\]", "").replaceAll("\\[\\(", "").replaceAll("\\[", "").replaceAll("\\]", "")
        .replaceAll("%", "percecnt").replaceAll("≥", "greaterThanOrEqualTo").replaceAll(">","greaterThan")
        .replaceAll("(?i)(TNBC)","").replaceAll("(?i)(BCa)","").replaceAll("(?i)(BC)","")
        .replaceAll("(?i)TNBC","Triple-Negative Breast Cancer").replaceAll("(?i)BCa","Breast Cancer").replaceAll("(?i)BC","Breast Cancer")
        .replaceAll("•","").replaceAll("‑","-")
        .replaceAll("Γ","Gamma").replaceAll("γ", "gamma")
        .replaceAll("Α","Alpha").replaceAll("α","alpha")
        .replaceAll("χ", "Chi")
        .replaceAll("ĸ", "Kra")
        .replaceAll("β","beta").replaceAll("Β","Beta")
      //println(c)

      val t = c.split(",")
      //t.foreach(println)
      if(t.length>5){
        println(t.length+"\n")
        t.foreach(println)
      }

      (t(0),t(1),t(2),t(4))
      //t
    }).filter(trip => {
      //I don't think this is doing anything
      (!trip._1.contains("\\d+"))&&(!trip._2.contains("\\d+"))&&(!trip._3.contains("\\d+"))&&(!trip._1.contains("[0-9]+"))&&(!trip._2.contains("[0-9]+"))&&(!trip._3.contains("[0-9]+"))
    })


    val subjectsReport = triplets.map(t => prepString(t._1)+","+t._4).distinct()
    val predicatesReport = triplets.map(t => prepString(t._2)+","+t._4).distinct()
    val objectsReport = triplets.map(t => prepString(t._3)+","+t._4).distinct()

    val pwSubRep = new PrintWriter(new File("output\\ReportSubjects.txt"))
    pwSubRep.write(subjectsReport.collect().mkString("\n"))
    pwSubRep.close

    val pwObjRep = new PrintWriter(new File("output\\ReportObjects.txt"))
    pwObjRep.write(objectsReport.collect().mkString("\n"))
    pwObjRep.close

    val pwPredRep = new PrintWriter(new File("output\\ReportPredicates.txt"))
    pwPredRep.write(predicatesReport.collect().mkString("\n"))
    pwPredRep.close

    val subjects = triplets.map(t => t._1).distinct().cache
    val predicates = triplets.map(t => t._2).distinct().cache
    val objects = triplets.map(t => t._3).distinct().cache

    val predOut = predicates.map(s => prepString(s))

    val subOut = subjects.map(s => prepString(s))

    val objOut = objects.map(s => prepString(s))

    val tripletsOut = triplets.distinct().map( s => {
      val ret = prepString(s._1) + "," + prepString(s._2) + "," + prepString(s._3) + ",Obj"
      ret
    })

    val pwPred = new PrintWriter(new File("output\\Predicates.txt"))
    pwPred.write(predOut.collect().mkString("\n"))
    pwPred.close

    val pwSub = new PrintWriter(new File("output\\Subjects.txt"))
    pwSub.write(subOut.collect().mkString("\n"))
    pwSub.close

    val pwObj = new PrintWriter(new File("output\\Objects.txt"))
    pwObj.write(objOut.collect().mkString("\n"))
    pwObj.close

    val pwTrip = new PrintWriter(new File("output\\Triplets.txt"))
    pwTrip.write(tripletsOut.collect().mkString("\n"))
    pwTrip.close


    val pwPreds = new PrintWriter(new File("finalOutput\\Triplets"))
    pwPreds.write(tripletsOut.distinct().collect().mkString("\n"))
    pwPreds.close


    if (args.length < 2) {
      System.out.println("\n$ java RESTClientGet [Bioconcept] [Inputfile] [Format]")
      System.out.println("\nBioconcept: We support five kinds of bioconcepts, i.e., Gene, Disease, Chemical, Species, Mutation. When 'BioConcept' is used, all five are included.\n\tInputfile: a file with a pmid list\n\tFormat: PubTator (tab-delimited text file), BioC (xml), and JSON\n\n")
    }
    else {
      val Bioconcept = args(0)
      val Inputfile = args(1)
      var Format = "PubTator"
      if (args.length > 2) {
        Format = args(2)
      }
      val medWords = ListBuffer.empty[(String, String)]

      for (line <- Source.fromFile(Inputfile).getLines) {
        val data = scala.io.Source.fromURL("https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/RESTful/tmTool.cgi/" + Bioconcept + "/" + line + "/" + Format + "/").getLines()

        val lines = data.flatMap(line => {
          line.split("\n")
        }).drop(2)
        //lines.foreach{l =>print(l+"\n")}

        val words = lines.flatMap(word => {
          word.split("\t").drop(3).dropRight(1)
        }).toArray
        //words.foreach{w =>print(w+"\n")}
        for (i <- 0 until words.length by 2) {
          if (i < words.length - 1) {
            val work = (words(i), words(i + 1))
            medWords += work
          }
        }
      }
      val mw = sc.parallelize(medWords.toList).distinct()

      val disW = mw.filter(w => w._2.equals("Disease")).map(w => w._1).collect.toSet
      val specW = mw.filter(w => w._2.equals("Species")).map(w => w._1).collect.toSet
      val geneW = mw.filter(w => w._2.equals("Gene")).map(w => w._1).collect.toSet
      val chemW = mw.filter(w => w._2.equals("Chemical")).map(w => w._1).collect.toSet
      val mutW = mw.filter(w => w._2.equals("Mutation")).map(w => w._1).collect.toSet

      val subDis = subjects.filter(s => {
        disW.contains(s)
      }).map(s => "Disease,"+prepString(s))
      val subSpec = subjects.filter(s => {
        specW.contains(s)
      }).map(s => "Species,"+prepString(s))
      val subGene = subjects.filter(s => {
        geneW.contains(s)
      }).map(s => "Gene,"+prepString(s))
      val subChem = subjects.filter(s => {
        chemW.contains(s)
      }).map(s => "Chemical,"+prepString(s))
      val subMut = subjects.filter(s => {
        mutW.contains(s)
      }).map(s => "Mutation,"+prepString(s))
      val subOth = subjects.filter(s => (!disW.contains(s))&&(!specW.contains(s))&&(!geneW.contains(s))&&(!chemW.contains(s))&&(!mutW.contains(s)))//.map(s => "Other,"+prepString(s))

      val otherSub = subOth.map(s => {
        //        println(s)
        //        var arr = s.split(" ")
        //        var isDis = false
        //        for(word <- arr){
        //          if(disW.contains(word)){
        //            isDis = true
        //          }
        //        }
        //        isDis
        var ret = ""
        var found = false
        var arr = s.split(" ")
        TFIDF_SynArr.foreach(a => {
          arr.foreach(w =>{
            if(a.contains(w)){
              found = true
              ret = a(0).capitalize + "," + prepString(s)
            }
          })
        })
        if(!found){
          ret = "Other,"+prepString(s)
        }
        ret
      }).collect.toSet

      otherSub.foreach(println)

      val otherSubU = subOth.map(s => {
        //        println(s)
        //        var arr = s.split(" ")
        //        var isDis = false
        //        for(word <- arr){
        //          if(disW.contains(word)){
        //            isDis = true
        //          }
        //        }
        //        isDis
        var ret = ""
        var found = false
        var arr = s.split(" ")
        TFIDF_SynArr.foreach(a => {
          arr.foreach(w =>{
            if(a.contains(w)){
              found = true
              ret = a(0).capitalize + "," + prepString(s)
            }
          })
        })
        if(!found){
          ret = "Other,"+prepString(s)
        }
        ret
      })

      val pwSubDis = new PrintWriter(new File("output\\SubjectDisease.txt"))
      pwSubDis.write(subDis.collect().mkString("\n"))
      pwSubDis.close

      val pwSubSpec = new PrintWriter(new File("output\\SubjectSpecies.txt"))
      pwSubSpec.write(subSpec.collect().mkString("\n"))
      pwSubSpec.close

      val pwSubChem = new PrintWriter(new File("output\\SubjectChemical.txt"))
      pwSubChem.write(subChem.collect().mkString("\n"))
      pwSubChem.close

      val pwSubGene = new PrintWriter(new File("output\\SubjectGene.txt"))
      pwSubGene.write(subGene.collect().mkString("\n"))
      pwSubGene.close

      val pwSubMut = new PrintWriter(new File("output\\SubjectMutation.txt"))
      pwSubMut.write(subMut.collect().mkString("\n"))
      pwSubMut.close

      val pwSubOth = new PrintWriter(new File("output\\SubjectOther.txt"))
      pwSubOth.write(otherSub.mkString("\n"))
      pwSubOth.close



      val objDis = objects.filter(s => {
//        println(s)
//        var arr = s.split(" ")
//        var isDis = false
//        for(word <- arr){
//          if(disW.contains(word)){
//            isDis = true
//          }
//        }
//        isDis
        disW.contains(s)
      }).map(s => "Disease,"+prepString(s))
      val objSpec = objects.filter(s => {
        specW.contains(s)
      }).map(s => "Species,"+prepString(s))
      val objGene = objects.filter(s => {
        geneW.contains(s)
      }).map(s => "Gene,"+prepString(s))
      val objChem = objects.filter(s => {
        chemW.contains(s)
      }).map(s => "Chemical,"+prepString(s))
      val objMut = objects.filter(s => {
        mutW.contains(s)
      }).map(s => "Mutation,"+prepString(s))
      val objOth = objects.filter(s => (!disW.contains(s))&&(!specW.contains(s))&&(!geneW.contains(s))&&(!chemW.contains(s))&&(!mutW.contains(s)))//.map(s => "Other,"+prepString(s))

      val otherObj = objOth.map(s => {
        //        println(s)
        //        var arr = s.split(" ")
        //        var isDis = false
        //        for(word <- arr){
        //          if(disW.contains(word)){
        //            isDis = true
        //          }
        //        }
        //        isDis
        var ret = ""
        var found = false
        var arr = s.split(" ")
        TFIDF_SynArr.foreach(a => {
          arr.foreach(w =>{
            if(a.contains(w)){
              found = true
              ret = a(0).capitalize + "," + prepString(s)
            }
          })
        })
        if(!found){
          ret = "Other,"+prepString(s)
        }
        ret
      }).collect.toSet

      otherObj.foreach(println)

      val otherObjU = objOth.map(s => {
        var ret = ""
        var found = false
        var arr = s.split(" ")
        TFIDF_SynArr.foreach(a => {
          arr.foreach(w =>{
            if(a.contains(w)){
              found = true
              ret = a(0).capitalize + "," + prepString(s)
            }
          })
        })
        if(!found){
          ret = "Other,"+prepString(s)
        }
        ret
      })

      val pwObjDis = new PrintWriter(new File("output\\objectDisease.txt"))
      pwObjDis.write(objDis.collect().mkString("\n"))
      pwObjDis.close

      val pwObjSpec = new PrintWriter(new File("output\\objectSpecies.txt"))
      pwObjSpec.write(objSpec.collect().mkString("\n"))
      pwObjSpec.close

      val pwObjChem = new PrintWriter(new File("output\\objectChemical.txt"))
      pwObjChem.write(objChem.collect().mkString("\n"))
      pwObjChem.close

      val pwObjGene = new PrintWriter(new File("output\\objectGene.txt"))
      pwObjGene.write(objGene.collect().mkString("\n"))
      pwObjGene.close

      val pwObjMut = new PrintWriter(new File("output\\objectMutation.txt"))
      pwObjMut.write(objMut.collect().mkString("\n"))
      pwObjMut.close

      val pwObjOth = new PrintWriter(new File("output\\objectOther.txt"))
      pwObjOth.write(otherObj.mkString("\n"))
      pwObjOth.close


      val individuals = subDis.union(subChem).union(subGene).union(subMut).union(subSpec).union(otherSubU)
        .union(objDis).union(objChem).union(objGene).union(objMut).union(objSpec).union(otherObjU)

      val pwIndividuals = new PrintWriter(new File("finalOutput\\Individuals"))
      pwIndividuals.write(individuals.distinct().collect().mkString("\n"))
      pwIndividuals.close

      val trips = triplets.distinct()

      val predDisDis = trips.filter(s => {
        disW.contains(s._1)&&disW.contains(s._3)
      }).map(s => prepString(s._2)+",Disease,Disease,Func")
      val predDisSpec = trips.filter(s => {
        disW.contains(s._1)&&specW.contains(s._3)
      }).map(s => prepString(s._2)+",Disease,Species,Func")
      val predDisGene = trips.filter(s => {
        disW.contains(s._1)&&geneW.contains(s._3)
      }).map(s => prepString(s._2)+",Disease,Gene,Func")
      val predDisChem = trips.filter(s => {
        disW.contains(s._1)&&chemW.contains(s._3)
      }).map(s => prepString(s._2)+",Disease,Chemical,Func")
      val predDisMut = trips.filter(s => {
        disW.contains(s._1)&&mutW.contains(s._3)
      }).map(s => prepString(s._2)+",Disease,Mutation,Func")
      val predDisOth = trips.filter(s => {
        disW.contains(s._1)&&(!disW.contains(s._3))&&(!specW.contains(s._3))&&(!geneW.contains(s._3))&&(!chemW.contains(s._3))&&(!mutW.contains(s._3))
      }).map(s => {
        var range = "Other"
        otherObj.foreach(a => {
          val arr = a.split(",")
          println(arr.length)
          if (arr.length > 1) {
            arr.foreach(println)
            if (arr(1).equals(prepString(s._3))) {
              range = arr(0)
            }
          }
        })
        prepString(s._2)+",Disease,"+range.capitalize+",Func"
      }).filter(a => {
        var ret = false
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true
        }
        ret
      })

      val predSpecSpec = trips.filter(s => {
        specW.contains(s._1)&&specW.contains(s._3)
      }).map(s => prepString(s._2)+",Species,Species,Func")
      val predSpecDis = trips.filter(s => {
        specW.contains(s._1)&&disW.contains(s._3)
      }).map(s => prepString(s._2)+",Species,Disease,Func")
      val predSpecGene = trips.filter(s => {
        specW.contains(s._1)&&geneW.contains(s._3)
      }).map(s => prepString(s._2)+",Species,Gene,Func")
      val predSpecChem = trips.filter(s => {
        specW.contains(s._1)&&chemW.contains(s._3)
      }).map(s => prepString(s._2)+",Species,Chemical,Func")
      val predSpecMut = trips.filter(s => {
        specW.contains(s._1)&&mutW.contains(s._3)
      }).map(s => prepString(s._2)+",Species,Mutation,Func")
      val predSpecOth = trips.filter(s => {
        specW.contains(s._1)&&(!disW.contains(s._3))&&(!specW.contains(s._3))&&(!geneW.contains(s._3))&&(!chemW.contains(s._3))&&(!mutW.contains(s._3))
      }).map(s => {
        var range = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._3))) {
              range = arr(0)
            }
          }
        })
        prepString(s._2)+",Species,"+range.capitalize+",Func"
      }).filter(a => {
        var ret = false
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true
        }
        ret
      })


      val predGeneGene = trips.filter(s => {
        geneW.contains(s._1)&&geneW.contains(s._3)
      }).map(s => prepString(s._2)+",Gene,Gene,Func")
      val predGeneDis = trips.filter(s => {
        geneW.contains(s._1)&&disW.contains(s._3)
      }).map(s => prepString(s._2)+",Gene,Disease,Func")
      val predGeneSpec = trips.filter(s => {
        geneW.contains(s._1)&&specW.contains(s._3)
      }).map(s => prepString(s._2)+",Gene,Species,Func")
      val predGeneChem = trips.filter(s => {
        geneW.contains(s._1)&&chemW.contains(s._3)
      }).map(s => prepString(s._2)+",Gene,Chemical,Func")
      val predGeneMut = trips.filter(s => {
        geneW.contains(s._1)&&mutW.contains(s._3)
      }).map(s => prepString(s._2)+",Gene,Mutation,Func")
      val predGeneOth = trips.filter(s => {
        geneW.contains(s._1)&&(!disW.contains(s._3))&&(!specW.contains(s._3))&&(!geneW.contains(s._3))&&(!chemW.contains(s._3))&&(!mutW.contains(s._3))
      }).map(s =>{
        var range = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._3))) {
              range = arr(0)
            }
          }
        })
        prepString(s._2)+",Gene,"+range.capitalize+",Func"
      }).filter(a => {
        var ret = false
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true
        }
        ret
      })

      val predChemChem = trips.filter(s => {
        chemW.contains(s._1)&&chemW.contains(s._3)
      }).map(s => prepString(s._2)+",Chemical,Chemical,Func")
      val predChemDis = trips.filter(s => {
        chemW.contains(s._1)&&disW.contains(s._3)
      }).map(s => prepString(s._2)+",Chemical,Disease,Func")
      val predChemSpec = trips.filter(s => {
        chemW.contains(s._1)&&specW.contains(s._3)
      }).map(s => prepString(s._2)+",Chemical,Species,Func")
      val predChemGene = trips.filter(s => {
        chemW.contains(s._1)&&geneW.contains(s._3)
      }).map(s => prepString(s._2)+",Chemical,Gene,Func")
      val predChemMut = trips.filter(s => {
        chemW.contains(s._1)&&mutW.contains(s._3)
      }).map(s => prepString(s._2)+",Chemical,Mutation,Func")
      val predChemOth = trips.filter(s => {
        chemW.contains(s._1)&&(!disW.contains(s._3))&&(!specW.contains(s._3))&&(!geneW.contains(s._3))&&(!chemW.contains(s._3))&&(!mutW.contains(s._3))
      }).map(s =>{
        var range = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._3))) {
              range = arr(0)
            }
          }
        })
        prepString(s._2)+",Chemical,"+range.capitalize+",Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })


      val predMutMut = trips.filter(s => {
        mutW.contains(s._1)&&mutW.contains(s._3)
      }).map(s => prepString(s._2)+",Mutation,Mutation,Func")
      val predMutDis = trips.filter(s => {
        mutW.contains(s._1)&&disW.contains(s._3)
      }).map(s => prepString(s._2)+",Mutation,Disease,Func")
      val predMutSpec = trips.filter(s => {
        mutW.contains(s._1)&&specW.contains(s._3)
      }).map(s => prepString(s._2)+",Mutation,Species,Func")
      val predMutGene = trips.filter(s => {
        mutW.contains(s._1)&&geneW.contains(s._3)
      }).map(s => prepString(s._2)+",Mutation,Gene,Func")
      val predMutChem = trips.filter(s => {
        mutW.contains(s._1)&&chemW.contains(s._3)
      }).map(s => prepString(s._2)+",Mutation,Chemical,Func")
      val predMutOth = trips.filter(s => {
        mutW.contains(s._1)&&(!disW.contains(s._3))&&(!specW.contains(s._3))&&(!geneW.contains(s._3))&&(!chemW.contains(s._3))&&(!mutW.contains(s._3))
      }).map(s =>{
        var range = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._3))) {
              range = arr(0)
            }
          }
        })
        prepString(s._2)+",Mutation,"+range.capitalize+",Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })


      val predOthDis = trips.filter(s => {
        (!disW.contains(s._1))&&(!specW.contains(s._1))&&(!geneW.contains(s._1))&&(!chemW.contains(s._1))&&(!mutW.contains(s._1))&&disW.contains(s._3)
      }).map(s => {
        var domain = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._1))) {
              domain = arr(0)
            }
          }
        })
        prepString(s._2)+","+domain.capitalize+",Disease,Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })
      val predOthSpec = trips.filter(s => {
        (!disW.contains(s._1))&&(!specW.contains(s._1))&&(!geneW.contains(s._1))&&(!chemW.contains(s._1))&&(!mutW.contains(s._1))&&specW.contains(s._3)
      }).map(s => {
        var domain = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._1))) {
              domain = arr(0)
            }
          }
        })
        prepString(s._2)+","+domain.capitalize+",Species,Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })
      val predOthGene = trips.filter(s => {
        (!disW.contains(s._1))&&(!specW.contains(s._1))&&(!geneW.contains(s._1))&&(!chemW.contains(s._1))&&(!mutW.contains(s._1))&&geneW.contains(s._3)
      }).map(s => {
        var domain = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._1))) {
              domain = arr(0)
            }
          }
        })
        prepString(s._2)+","+domain.capitalize+",Gene,Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })
      val predOthChem = trips.filter(s => {
        (!disW.contains(s._1))&&(!specW.contains(s._1))&&(!geneW.contains(s._1))&&(!chemW.contains(s._1))&&(!mutW.contains(s._1))&&chemW.contains(s._3)
      }).map(s => {
        var domain = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._1))) {
              domain = arr(0)
            }
          }
        })
        prepString(s._2)+","+domain.capitalize+",Chemical,Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })
      val predOthMut = trips.filter(s => {
        (!disW.contains(s._1))&&(!specW.contains(s._1))&&(!geneW.contains(s._1))&&(!chemW.contains(s._1))&&(!mutW.contains(s._1))&&mutW.contains(s._3)
      }).map(s => {
        var domain = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._1))) {
              domain = arr(0)
            }
          }
        })
        prepString(s._2)+","+domain.capitalize+",Mutation,Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })
      val predOthOth = trips.filter(s => {
        (!disW.contains(s._1))&&(!specW.contains(s._1))&&(!geneW.contains(s._1))&&(!chemW.contains(s._1))&&(!mutW.contains(s._1))&&(!disW.contains(s._3))&&(!specW.contains(s._3))&&(!geneW.contains(s._3))&&(!chemW.contains(s._3))&&(!mutW.contains(s._3))
      }).map(s =>{
        var domain = "other"
        var range = "Other"
        otherObj.foreach(a =>{
          val arr = a.split(",")
          if (arr.length > 1) {
            if (arr(1).equals(prepString(s._1))) {
              domain = arr(0)
            }
            if (arr(1).equals(prepString(s._3))) {
              range = arr(0)
            }
          }
        })
        prepString(s._2)+","+domain.capitalize+","+range.capitalize+",Func"
      }).filter(a => {
        var ret = false;
        var arr = a.split(",")
        if(!(arr(0)=="")){
          ret = true;
        }
        ret
      })

      val preds = predDisDis.union(predDisSpec).union(predDisGene).union(predDisChem).union(predDisOth).union(predDisOth)
        .union(predSpecDis).union(predSpecChem).union(predSpecGene).union(predSpecMut).union(predSpecOth).union(predSpecSpec)
        .union(predGeneDis).union(predGeneSpec).union(predGeneGene).union(predGeneChem).union(predGeneMut).union(predGeneOth)
        .union(predChemDis).union(predChemSpec).union(predChemGene).union(predChemChem).union(predChemMut).union(predChemOth)
        .union(predMutDis).union(predMutSpec).union(predMutGene).union(predMutChem).union(predMutMut).union(predMutOth)
        .union(predOthDis).union(predOthSpec).union(predOthGene).union(predOthChem).union(predOthMut).union(predOthOth)

      val pwPreds = new PrintWriter(new File("finalOutput\\ObjectProperties"))
      pwPreds.write(preds.distinct().collect().mkString("\n"))
      pwPreds.close

      val pwPredDis = new PrintWriter(new File("output\\predDis.txt"))
      pwPredDis.println(predDisDis.distinct().collect().mkString("\n"))
      pwPredDis.println(predDisSpec.distinct().collect().mkString("\n"))
      pwPredDis.println(predDisGene.distinct().collect().mkString("\n"))
      pwPredDis.println(predDisChem.distinct().collect().mkString("\n"))
      pwPredDis.println(predDisMut.distinct().collect().mkString("\n"))
      pwPredDis.println(predDisOth.distinct().collect().mkString("\n"))
      pwPredDis.close

      val pwPredSpec = new PrintWriter(new File("output\\predSpec.txt"))
      pwPredSpec.println(predSpecDis.distinct().collect().mkString("\n"))
      pwPredSpec.println(predSpecSpec.distinct().collect().mkString("\n"))
      pwPredSpec.println(predSpecGene.distinct().collect().mkString("\n"))
      pwPredSpec.println(predSpecChem.distinct().collect().mkString("\n"))
      pwPredSpec.println(predSpecMut.distinct().collect().mkString("\n"))
      pwPredSpec.println(predSpecOth.distinct().collect().mkString("\n"))
      pwPredSpec.close

      val pwPredGene = new PrintWriter(new File("output\\predGene.txt"))
      pwPredGene.println(predGeneDis.distinct().collect().mkString("\n"))
      pwPredGene.println(predGeneSpec.distinct().collect().mkString("\n"))
      pwPredGene.println(predGeneGene.distinct().collect().mkString("\n"))
      pwPredGene.println(predGeneChem.distinct().collect().mkString("\n"))
      pwPredGene.println(predGeneMut.distinct().collect().mkString("\n"))
      pwPredGene.println(predGeneOth.distinct().collect().mkString("\n"))
      pwPredGene.close

      val pwPredChem = new PrintWriter(new File("output\\predChem.txt"))
      pwPredChem.println(predChemDis.distinct().collect().mkString("\n"))
      pwPredChem.println(predChemSpec.distinct().collect().mkString("\n"))
      pwPredChem.println(predChemGene.distinct().collect().mkString("\n"))
      pwPredChem.println(predChemChem.distinct().collect().mkString("\n"))
      pwPredChem.println(predChemMut.distinct().collect().mkString("\n"))
      pwPredChem.println(predChemOth.distinct().collect().mkString("\n"))
      pwPredChem.close

      val pwPredMut = new PrintWriter(new File("output\\predMut.txt"))
      pwPredMut.println(predMutDis.distinct().collect().mkString("\n"))
      pwPredMut.println(predMutSpec.distinct().collect().mkString("\n"))
      pwPredMut.println(predMutGene.distinct().collect().mkString("\n"))
      pwPredMut.println(predMutChem.distinct().collect().mkString("\n"))
      pwPredMut.println(predMutMut.distinct().collect().mkString("\n"))
      pwPredMut.println(predMutOth.distinct().collect().mkString("\n"))
      pwPredMut.close

      val pwPredOth = new PrintWriter(new File("output\\predOth.txt"))
      pwPredOth.println(predOthDis.distinct().collect().mkString("\n"))
      pwPredOth.println(predOthSpec.distinct().collect().mkString("\n"))
      pwPredOth.println(predOthGene.distinct().collect().mkString("\n"))
      pwPredOth.println(predOthChem.distinct().collect().mkString("\n"))
      pwPredOth.println(predOthMut.distinct().collect().mkString("\n"))
      pwPredOth.println(predOthOth.distinct().collect().mkString("\n"))
      pwPredOth.close


    }
  }

  def prepString(s: String): String = {
    var temp = s
    if(s.contains(" ")) {
      val words = s.toLowerCase().split(" ")
      for(i <- 1 until words.length)
      {
        words(i) = words(i).capitalize
      }
      temp = words.mkString("").replaceAll("[.]", "")
    }
    temp
  }


}
