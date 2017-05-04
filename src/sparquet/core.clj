(ns sparquet.core
  (:require [flambo.api :as f]
            [flambo.tuple :as ft])
  (:import [org.apache.parquet.hadoop.thrift
            ThriftReadSupport
            ParquetThriftInputFormat
            ParquetThriftOutputFormat]
           [org.apache.hadoop.mapreduce Job]))

(defn read-parquet
  [spark-context path thrift-class & args]
  (let [args-map (apply array-map args)
        job (Job/getInstance (.hadoopConfiguration spark-context))]
    (when-let [projection (:projection args-map)]
      (doto (.getConfiguration job)
        (.set ThriftReadSupport/THRIFT_COLUMN_FILTER_KEY
              projection)))
    (when-let [filter (:filter args-map)]
      (ParquetThriftInputFormat/setFilterPredicate
       (.getConfiguration job)
       filter))
    (-> (.newAPIHadoopFile spark-context path
                           ParquetThriftInputFormat
                           Void thrift-class
                           (.getConfiguration job))
        f/values)))

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
    (let [job (-> rdd
                  .context
                  .hadoopConfiguration
                  Job/getInstance)]
      (ParquetThriftOutputFormat/setThriftClass job thrift-class)
      (.saveAsNewAPIHadoopFile rdd path
                               Void thrift-class
                               ParquetThriftOutputFormat
                               (.getConfiguration job)))))
