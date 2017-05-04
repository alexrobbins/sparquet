(defproject sparquet "0.1.1-SNAPSHOT"
  :description "Easy integration for thrift defined parquet and Flambo."
  :url "https://github.com/alexrobbins/sparquet"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure             "1.8.0"]
                 [yieldbot/flambo                 "0.8.0"]
                 [org.apache.parquet/parquet-thrift "1.8.1"]]
  :aot [sparquet.core]
  :profiles {:provided
             {:dependencies
              [[org.apache.spark/spark-core_2.11 "2.1.1"]
               [org.apache.thrift/libthrift "0.9.2"
                :exclusions [org.slf4j/slf4j-api]]]}
             :uberjar
             {:aot :all}
             :dev
             {:plugins [[lein-thriftc "0.2.1"]]
              :hooks [leiningen.thriftc]
              :thriftc {:source-paths ["test/thrift"]}}})
