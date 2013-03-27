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
  [level-db-root-dir options ar-name]
  (open-leveldb (str level-db-root-dir ar-name) options))

(defn store-events-id-mapped-by-ar-id
  [ar-name ar-id events db]
  (let [batch-process (.createWriteBatch db)
        ar-name-str (name ar-name)
        ar-id-str (str ar-id)
        charset "UTF-8"
        current-eventids (.get batch-process (to-bytes ar-id-str charset))
        appended-events (str current-eventids "," ar-id-str)]
   (.put batch-process (to-bytes ar-id-str charset) (to-bytes appended-events charset))
   (.write db batch-process)))

(defn store-uncommitted-events
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc "store the events of aggregate root"}
  [ar-name ar-id events open-db]
  (let [ar-name-str (name ar-name)
        key (str ar-id)]
    ()))


