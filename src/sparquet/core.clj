(ns sparquet.core
  (:require [flambo.api :as f]
            [flambo.tuple :as ft])
  (:import [parquet.hadoop.thrift ParquetThriftInputFormat
                                  ParquetThriftOutputFormat]
           [org.apache.hadoop.mapreduce Job]))

(defn read-parquet
  [spark-context path thrift-class]
  (-> (.newAPIHadoopFile spark-context path
                         ParquetThriftInputFormat
                         Void thrift-class
                         (.getConfiguration (Job.)))
      f/values))

(defprotocol ParquetWritable
  (write-parquet [rdd path thrift-class]
    "Writes the RDD as parquet."))

(extend-protocol ParquetWritable
  org.apache.spark.api.java.JavaRDD
  (write-parquet [rdd path thrift-class]
    (-> rdd
        (f/map-to-pair (f/fn [obj] (ft/tuple nil obj)))
        (write-parquet path thrift-class)))

  org.apache.spark.api.java.JavaPairRDD
  (write-parquet [rdd path thrift-class]
    (let [job (Job.)]
      (ParquetThriftOutputFormat/setThriftClass job thrift-class)
      (.saveAsNewAPIHadoopFile rdd path
                               Void thrift-class
                               ParquetThriftOutputFormat
                               (.getConfiguration job)))))