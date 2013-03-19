(ns raiseup.ontime.commandhandler
  (:require [clojure.data.json :as json]))

(def fixed-length-of-store-key 20)
(def padded-char \space)
(def fixed-length-of-ar-name-length 2)

(defn pad-number
  [number fixed-length padded-num]
  (let [number-as-str (str number)
        num-of-padded-num (- fixed-length (.length number-as-str))]
    (if (< 0 num-of-padded-num)
      (str (apply str (repeat num-of-padded-num padded-num)) number-as-str)
      number-as-str)))

(defn construct-store-key
  [ar-name ar-id]
  (let [ar-name-length (.length ar-name)]
    (str (pad-number ar-name-length fixed-length-of-ar-name-length 0) ar-name ar-id)))

(defn store-aggregate-root
  ^{:added "1.0"
    :abbre "ar->aggregate root name"}
  [db aggregate-root]
  ())

(defn transact
  ^{:added "0.1"
    :abbre "fun=>function"}
  [db fun]
   (try (fun db)
         (finally (.close db))))

(defn to-bytes
  [string charset]
  (.getBytes string charset))


(defn process-kv
  ^{:added "1.0"
    :abbre "(kv:key value)"
    :doc "process key value pair,eg. push them to db
          and pre-fun is used to pre process key value before processing"}
  ([key value pre-fun]
     (fn [db-fun] (db-fun (pre-fun key) (pre-fun value)))))

(defn process-string-kv
  "process key value which is string"
  ([key value charset]
      (process-kv key value #(to-bytes % charset)))
  ([key value]
      (process-string-kv key value "UTF-8")))

(defn write-to-leveldb
  ^{:added "1.0"
    :doc "write key value to level db"}
  ([db key value charset]
       (transact db
              (fn [db1]
                ((process-string-kv key value charset)
                 (fn [key1 value1] (.put db1 key1 value1))))))
  ([db key value]
     (write-to-leveldb db key value "UTF-8")))

(defn open-leveldb
 ^{:added "1.0"
   :doc   "initialize or open the level db for using"
   :side-affect "true"
   }
  [db-dir options]
  (let [leveldb-options (org.iq80.leveldb.Options.)
        file (java.io.File. db-dir)]
    (.open (org.fusesource.leveldbjni.JniDBFactory/factory) file leveldb-options)))
