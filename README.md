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

### Predicate Pushdown

Parquet supports predicate pushdown. You can provide a predicate
that Parquet runs while iterating over your records. Parquet
keeps some simple statistics on the blocks it writes, so predicates
can skip whole sections of records without deserialization. Big
performance win.

Use the `pred` macro in
[`sparquet.filter`](src/sparquet/filter.clj)
to set up your predicates.  Be careful to match the types of your
Thrift schema with the values you provide in the filters. Things like
long/int mismatches will cause exceptions when running the job.

When using a predicate the arguments should be a column name and the
comparison value. The type of the column is found from the type of
value you pass.

Valid predicates: `= not= > >= < <= and not or`.

```clojure
(ns sparquet.example
  (:require [flambo.api :as f]
            [sparquet.core :refer [read-parquet write-parquet]]
            [sparquet.filter :refer [pred]])
  (:import [sparquet.test Name]))

(defonce sc (f/local-spark-context "sparquet-testing"))

(def id-is-1 (pred (= "id" (int 1)))  ;;coerce to avoid int/long mismatch

(read-parquet sc path Name :filter id-is-1)
```

#### Nils

* `nil` can only be passed to `=` or `not=`.
* `nil` is `=` to `nil` and `not=` to everything else.
* All other predicates drop rows with `nil`, since `nil` isn't `Comparable`.
* Set the column type manually, since `nil` doesn't provide a type.

```clojure
(ns sparquet.example
  (:require [flambo.api :as f]
            [sparquet.core :refer [read-parquet write-parquet]]
            [sparquet.filter :refer [pred]])
  (:import [sparquet.test Name]))

(def fname-is-nil
     (pred (= (f/string-column "fname") nil)))

(read-parquet sc path Name :filter fname-is-nil)
```

#### ParquetValue protocol

The filter system uses the
`sparquet.filter/ParquetValue` protocol to convert its
input into a Parquet recognized type. You can extend the protocol with
any type as long as it can be mapped into one of the existing column
types.

### Projection

Parquet also supports projections (in the relational algebra
sense). Many data jobs require only a subset of an object's fields. For
example, if we wanted only the `id` and `first_name` fields of a
larger Name object, we could pass a projection string to specify the
fields we cared about.

```clojure
(ns sparquet.example
  (:require [flambo.api :as f]
            [sparquet.core :refer [read-parquet write-parquet]])
  (:import [sparquet.test Name]))

(read-parquet sc path Name :projection "id;first_name"))
```

A more complex projection string and the fields it'd include:

`"a/**;b;c/*;d/{e,f}"`

* All fields under a
* b
* c's direct children
* e and f under d

Unprojected fields' behavior depends on the Thrift schema. `optional`
fields are just dropped. `required` fields are initialized to some
kind of type aware empty value. 0 for ints, empty string for strings,
etc.

## License

Copyright © 2015 Alex Robbins

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
