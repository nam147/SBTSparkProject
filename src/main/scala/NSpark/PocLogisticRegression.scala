package com.namspark.scala

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.{Row, SQLContext}


object PocLogisticRegression {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("PocLogisticRegression")
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

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(10)
      .run(trainingData)

    // Compute raw scores on the test set.
    val predictionAndLabels = testingData.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Evaluation metrics to score the algorithme:
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy
    val precision = metrics.weightedPrecision
    val recall = metrics.weightedRecall
    println(s"Accuracy = $accuracy")
    println(s"Weighted Precision = $precision")
    println(s"Weighted Precision = $recall")

    sc.stop()
  }
}