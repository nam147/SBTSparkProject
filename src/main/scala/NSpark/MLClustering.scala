/*voici le code que j'ai effectue au cours de mon stage a scaled risk, dans */
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.{Row, SQLContext}

object MLClustering {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val clusteringData = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true") // utiliser la 1ere ligne de donnee comme nom des colonnes
      .option("inferSchema", "true") // deviner quelle colonne de la donnee est numerique et quelle colonne est categorique
      .load("~/DataFile.csv") // le fichier data a ete anonymise
      .rdd
      .map{ row:Row => Vectors.dense(row.getAs[Double](0), row.getAs[Double](1), row.getAs[Double](2))}


    val k = 15
    val numIterations = 100
    val kmeans = new KMeans()
      .setSeed(174L)
      .setK(k)
      .setMaxIterations(numIterations)

    val model = kmeans.run(clusteringData)
    //Compute the Within Cluster Square Sum
    val WCSSE = model.computeCost(clusteringData)
    println("cluster centers")
    model.clusterCenters.foreach(println)
    println(" Squ. Root of Within Set Sum of Squared Errors = " + Math.sqrt(WCSSE))
    sc.stop()
  }
}