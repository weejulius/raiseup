(ns raiseup.base
  (:use [clojure.string :only (join)])
  (:require [raiseup.mutable :as mutable]
            [taoensso.nippy :as nippy]
            [cheshire.core :as json]))

(defn join-str
  "join a bunch of items with separator
   eg. (join-str ',' [1 3]) => 1,3 "
  ([separator prefix coll]
     (join separator (cons prefix coll))))


(defn to-bytes
  "convert string to bytes"
  ([string charset]
     (if (nil? string)
       nil
      (.getBytes string charset)))
  ([string]
     (to-bytes string mutable/charset)))

(defn process-kv
  ^{:added "1.0"
    :abbre "kv => a pair of key value"
    :doc "process key value pair,eg. push them to storage
          and pre-fun is used to pre process key value before storage"}
  ([key value process pre-fun]
    (process (pre-fun key) (pre-fun value))))


(defn data->bytes
  "convert data to bytes"
  [data]
  (nippy/freeze-to-bytes data))

(defn bytes->data
  "convert bytes to data"
  [bytes]
  (nippy/thaw-from-bytes bytes))

(defn int-to-bytes
  "convert the int collection to bytes"
  [int-coll]
  (if (nil? int-coll)
    nil
    (let [size (count int-coll)
          buffer (java.nio.ByteBuffer/allocate (* size 4))]
      (doseq [int int-coll]
        (.putInt buffer int))
      (.array buffer))))

(defn byte-to-int-array
  "convert bytes to int collection"
  [int-bytes]
  (if (nil? int-bytes)
    nil
    (let [buffer (java.nio.ByteBuffer/wrap int-bytes)
          size (/ (count int-bytes) 4)]
      (repeatedly size #(.getInt buffer)))))


(defn long->bytes
  [l]
  (if (nil? l) nil
      (-> (java.nio.ByteBuffer/allocate 8)
           (.putLong l)
           (.array))))

(defmulti ->long
  "convert to long from other types" class)

(defmethod ->long String
  [str]
  (Long/parseLong str))

(defmethod ->long (class nil)
  [p]
  nil)

(defmethod ->long
  (Class/forName "[B")
  [bytes]
 (->> (java.nio.ByteBuffer/wrap bytes)
       (.getLong)))

(defmulti ->map
  "convert to map data structure from string or bytes,etc" class)

(defmethod ->map
  String
  [str]
  (json/parse-string str))


(defmulti ->str
  "convert to str from ds,bytes etc" class)

(defmethod ->str
  (class {})
  [a-map]
  (json/generate-string a-map))
