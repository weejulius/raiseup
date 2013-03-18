(ns raiseup.ontime.commandhandler
  (:require [clojure.data.json :as json]))

(def fixed-length-of-store-key 20)
(def padded-char \space)

(defn pad-aggregate-root-name
  "pad a string to a fixed length with char, for example
   pad the string 'task' to the string whose length is 10 with space
   ,like 'task      '"
  [ar-name fix-length padded-char]
  (let [ar-length (.length ar-name)
        num-of-padded-chars (- fix-length ar-length)]
    (if(<= num-of-padded-chars 0)
      (subs ar-name 0 fix-length)
      (str ar-name (apply str (repeat num-of-padded-chars padded-char))))))

(defn store-aggregate-root
  ^{:added "1.0"
    :abbre "ar->aggregate name"}
  [db aggregate-root]
  (let [ar-name (name (:ar-name aggregate-root))
        store-key (str (pad-aggregate-root-name
                         ar-name fixed-length-of-store-key padded-char)
                       (:id aggregate-root))
        store-value (:events aggregate-root)]
    (write-to-leveldb db store-key store-value)))

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
