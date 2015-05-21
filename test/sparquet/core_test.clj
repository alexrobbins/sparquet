(ns sparquet.core-test
  (:require [clojure.test :refer :all]
            [sparquet.core :refer :all]
            [clojure.java.shell :refer [sh]]
            [flambo.api :as f])
  (:import [sparquet.test Address
                          Name
                          Person]))

(defonce sc (f/local-spark-context "sparquet-testing"))

(defn make-name [id fname & [lname]]
  (let [name (Name. id fname)]
    (when lname
      (.setLast_name name lname))
    name))

(defn make-names [& names]
  (mapv #(apply make-name %) names))

(def names
  (make-names
   [1 "A" "Lastname"]
   [2 "B" "Lastname"]
   [3 "C" nil]))


(deftest roundtrip-test
  (let [tmp-path "/tmp/sparquet-test/test.parquet"]
    ;; try to delete path, to clean up from earlier
    (sh "rm" "-rf" tmp-path)
    ;; write to temp path
    (-> (f/parallelize sc names)
        (write-parquet tmp-path Name))
    ;; read from temp path
    ;; compare names to data read from temp path
    (is
     (= (set names)
        (set (-> (read-parquet sc tmp-path Name)
                 f/collect))))
    (sh "rm" "-rf" tmp-path)))