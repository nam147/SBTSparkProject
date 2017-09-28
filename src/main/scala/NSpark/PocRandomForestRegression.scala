package com.namspark.scala

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.{DenseVector}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.sql.{Row, SQLContext}


object PocRandomForestRegression{
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("PocRFRegression")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    //Loading test and training data for Random Forest Regression
    val trainingData = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true")  // Premiere ligne comme nom des colonnes
      .option("inferSchema", "true") //Deviner le type de la donnee (categorique, numerique)
      .load("~/trainingDataFile.csv")
      .rdd
      .map({ row:Row => LabeledPoint(row.getDouble(8), new DenseVector(Array(row.getDouble(1),row.getDouble(2),row.getDouble(3),
        row.getDouble(4), row.getDouble(5), row.getDouble(6), row.getDouble(7)))) })

    val testingData = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("~/testingDataFile.csv")
      .rdd
      .map(row => LabeledPoint(row.getDouble(8), new DenseVector(Array(row.getDouble(1),row.getDouble(2),row.getDouble(3),
        row.getDouble(4), row.getDouble(5), row.getDouble(6), row.getDouble(7)))) )
    //Here we predict by regression the value of the last column, with the value of the 7 first columns

    // We can also split the data into test and training samples like so :
    // val splits = data.randomSplit(Array(0.7, 0.3))
    // val (trainingData, testData) = (splits(0), splits(1))

    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 500
    val featureSubsetStrategy = "auto"
    val impurity = "variance" // or "gini"
    val maxDepth = 4
    val maxBins = 32
    val seed = 579
    val model = RandomForest.trainRegressor(trainingData, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins, seed)

    // Evaluate model on test instances and compute test error with Mean Square Error
    val labelsAndPredictions = testingData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val MSEonTestData = labelsAndPredictions.map{ case(v, p) => math.pow((v - p), 2)}.mean()
    println("Mean Squared Error on Test Data= " + MSEonTestData)

    sc.stop()
  }
}