(ns sparquet.core-test
  (:require [clojure.test :refer :all]
            [sparquet.core :refer :all]
            [clojure.java.shell :refer [sh]]
            [flambo.api :as f]
            [sparquet.filter :as sf :refer [pred]])
  (:import [sparquet.test Address
                          Name
                          Person]))

(def tmp-path "/tmp/sparquet-test/test.parquet")

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

(defn clear-sample-data [f]
  (sh "rm" "-rf" tmp-path)
  (-> (f/parallelize sc names)
      (write-parquet tmp-path Name))
  (f)
  (sh "rm" "-rf" tmp-path))

(use-fixtures :once clear-sample-data)

(deftest roundtrip-test ;; written in fixture
  (is
   (= (set names)
      (set (-> (read-parquet sc tmp-path Name)
               f/collect)))))

(deftest filter-test
  (let [id-pred     (pred (= "id" (int 1)))
        id-pred-2   (pred (> "id" (int 1)))
        string-pred (pred (= "first_name" "A"))
        nil-pred    (pred (= (sf/binary-column "last_name") nil))
        nil-pred-2  (pred (= (sf/string-column "last_name") nil))]
    (are [filter output]
      (= (-> (read-parquet sc tmp-path Name :filter filter)
              f/collect)
         output)
      id-pred     [(first names)]
      id-pred-2   (rest names)
      string-pred [(first names)]
      nil-pred    [(last names)]
      nil-pred-2  [(last names)])))

(deftest projection-test
  (are [projection-string output]
    (= (-> (read-parquet sc tmp-path Name :projection projection-string)
           f/collect)
       output)
    "id;first_name" (make-names [1 "A"] [2 "B"] [3 "C"])
    "id"            (make-names [1 ""] [2 ""] [3 ""])
    "last_name"     (make-names [0 "" "Lastname"] [0 "" "Lastname"] [0 ""])))