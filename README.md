# sparquet

Easily read and write Thrift-defined Parquet files from Flambo (the Clojure spark dsl).

## Latest Version

The latest release version of parquet-thrift-cascalog is hosted on [Clojars](https://clojars.org):

[![Clojars Project](http://clojars.org/sparquet/latest-version.svg)](http://clojars.org/sparquet)

## Usage

```clojure
(ns sparquet.example
  (:require [flambo.api :as f]
            [sparquet.core :refer [read-parquet write-parquet]])
  (:import [sparquet.test Name]))

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

(let [tmp-path "/tmp/sparquet-test/test.parquet"]
  ;; write to temp path
  (-> (f/parallelize sc names)
      (write-parquet tmp-path Name))
  ;; read from temp path
  (-> (read-parquet sc tmp-path Name)
      f/collect))
```

## License

Copyright © 2015 Alex Robbins

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
