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

(defn write-to-leveldb
  ^{:added "1.0"
    :doc "write key value to level db"}
  ([db key value charset]
     (transact db
               (fn [db1]
                 (store-string-kv
                  key
                  value
                  (fn [key1 value1] (.put db1 key1 value1))
                  charset))))
  ([db key value]
     (write-to-leveldb db key value mutable/charset)))

(defn open-leveldb
  ^{:added "1.0"
    :doc   "initialize or open the level db to be used,
            the level db object is cached once it is opened"
    :side-affect "true"}
  ([db-dir options]
     (let [leveldb-options (org.iq80.leveldb.Options.)
           file (java.io.File. db-dir)
           existing-db (get @mutable/opened-leveldb db-dir)]
       (print @mutable/opened-leveldb)
       (if (nil? existing-db)
         (swap! mutable/opened-leveldb
                (fn [dbs]
                  (assoc dbs db-dir
                         (.open
                          (org.fusesource.leveldbjni.JniDBFactory/factory)
                          file
                          leveldb-options))))
         existing-db)))
  ([db-dir]
     (open-leveldb db-dir mutable/default-leveldb-option)))



(defn open-leveldb-for-ar
  ^{:doc "init and open the level db for ar"
    :abbre "ar=>aggregate root"}
  [level-db-root-dir options ar-name]
  (open-leveldb (str level-db-root-dir ar-name) options))


(defn find-value-by-key
  "find the value in the leveldb according to key"
  [key-str db]
  (.get db (base/to-bytes key-str)))



