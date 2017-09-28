============ READ ME PLEASE ===========
Ce README sert a expliquer la procedure de lancement d'un job spark
=======================================

apres avoir cloné le dossier git dans un dossier local 

mkdir SBTSparkProject && cd SBTSparkProject
git clone https://github.com/nam147/SBTSparkProject.git

Le fichier JAR qui est cense etre execute par la commande spark-submit pour lancer le job spark se trouve dans target/scala-2.10/src_managed
On execute les commandes suivantes pour uploader la donnée dans Hbase

hdfs dfs -mkdir /data
hdfs dfs -copyFromLocal Datafile ~/DataFile.csv /data/DataFile.csv

============ J'utilise spark 1.6.3 et Scala 2.10.6 =============
 
En fournissant databricks spark-csv a l'option package, on peut lire des csv et executer un job spark, nous avons choisi d'executer MLClustering
spark-submit --master local --class MLClustering --files /etc/hbase/conf/hbase-site.xml --packages com.databricks:spark-csv_2.10:1.5.0 /path/to/SBTSparkProject/target/scala-2.10/src_managed/sbtsparkproject_2.10-1.0.jar 



Pour creer le fichier jar, j'ai utilise Intellij, cree un Project SBT, cree le fichier build.sbt avec toutes les dependences puis j'ai cree le jar avec la commande :
sbt package

