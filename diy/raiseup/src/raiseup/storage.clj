(ns raiseup.storage
  (:require [raiseup.mutable :as mutable]
            [raiseup.base :as base]))

(defn transact
  ^{:added "0.1"
    :abbre "fun=>function"
    :doc "compose a transaction"}
  [db fun]
   (try (fun db)
         (finally (.close db))))


(defn store-string-kv
  "store key value which is string"
  ([key value store charset]
     (base/process-kv key value #(base/to-bytes % charset)))
  ([key value]
     (store-string-kv key value mutable/charset)))

(defn- open-new-leveldb
  [file leveldb-options]
  (try
    (.open
     (org.fusesource.leveldbjni.JniDBFactory/factory)
     file
     leveldb-options)
    (catch Exception e (println e))))

(defn open-leveldb
  ^{:added "1.0"
    :doc   "initialize or open the level db to be used,
            the level db object is cached once it is opened"
    :side-affect "true"}
  ([db-dir options]
     (if-let [existing-db (get @mutable/opened-leveldb db-dir)]
       existing-db
       (let [opened-new-db (open-new-leveldb
                            (java.io.File. db-dir)
                            (org.iq80.leveldb.Options.))]
         (swap! mutable/opened-leveldb
                (fn [dbs]
                  (assoc dbs db-dir opened-new-db)))
         opened-new-db)))
  ([db-dir]
     (open-leveldb db-dir mutable/default-leveldb-option)))


(defn find-value-by-key
  "find the value in the leveldb according to key"
  [key-str db]
  (.get db (base/to-bytes key-str)))



