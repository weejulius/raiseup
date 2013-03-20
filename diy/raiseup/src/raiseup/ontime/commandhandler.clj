(ns raiseup.ontime.commandhandler
  (:require [clojure.data.json :as json]))


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
   :side-affect "true"}
  [db-dir options]
  (let [leveldb-options (org.iq80.leveldb.Options.)
        file (java.io.File. db-dir)]
    (.open (org.fusesource.leveldbjni.JniDBFactory/factory) file leveldb-options)))

(defn open-leveldb-for-ar
  ^{:doc "init and open the level db for ar"
    :abbre "ar=>aggregate root"}
  [level-db-root-dir options]
  (fn [ar-name]
    (open-leveldb (str level-db-root-dir ar-name) options)))

(defn store-aggregate-root
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc "store the events of aggregate root"}
  [aggregate-root open-db]
  (let [ar-name-str (name (:ar-name aggregate-root))
        key (str (:ar-id aggregate-root))
        value (json/write-str (:events aggregate-root))]
    (write-to-leveldb
     (open-db ar-name-str)  key value)))


(defn read-aggregate-root
  [ar-name ar-id]
  ())
