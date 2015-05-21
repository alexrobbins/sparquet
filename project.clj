(defproject sparquet "0.1.1-SNAPSHOT"
  :description "Easy integration for thrift defined parquet and Flambo."
  :url "https://github.com/alexrobbins/sparquet"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure             "1.6.0"]
                 [yieldbot/flambo                 "0.5.0"]
                 [com.twitter/parquet-thrift "1.6.0"]]
  :aot [flambo.function]
  :profiles {:provided
             {:dependencies
              [[org.apache.spark/spark-core_2.10 "1.3.0"]
               [org.apache.thrift/libthrift "0.9.2"
                :exclusions [org.slf4j/slf4j-api]]]}
             :uberjar
             {:aot :all}
             :dev
             {:dependencies [[midje "1.6.3"]
                             [cascalog/midje-cascalog "2.1.1"]]
              :plugins [[lein-thriftc "0.2.1"]
                        [lein-midje "3.1.3"]]
              :hooks [leiningen.thriftc]
              :thriftc {:source-paths ["test/thrift"]}}})
