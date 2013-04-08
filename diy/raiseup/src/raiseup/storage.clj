(ns raiseup.storage
  (:require [raiseup.mutable :as mutable]
            [raiseup.base :as base]
            [clojure.java.io :as io]))

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
  "before using the leveldb open it"
  [file leveldb-options]
  (try
    (.open
     (org.fusesource.leveldbjni.JniDBFactory/factory)
     file
     leveldb-options)
    (catch Exception e
      (do (println "the db is not able be opened by " e ",let us repair and retry")
          (.repair
           (org.fusesource.leveldbjni.JniDBFactory/factory)
           file
           leveldb-options)
          (.open
           (org.fusesource.leveldbjni.JniDBFactory/factory)
           file
           leveldb-options)))))



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

(defn destroy-leveldb
  ([file options]
     (.destroy org.fusesource.leveldbjni.JniDBFactory/factory
               (io/file file)
               (org.iq80.leveldb.Options.)))
  ([file]
     (destroy-leveldb file mutable/default-leveldb-option)))


(defn k->value
  [key-byte db]
  (.get db key-byte))

(defn find-value-by-key
  "find the value in the leveldb according to key"
  [key-str db]
  (k->value (base/to-bytes key-str) db))

(defn- write
  [key-bytes value-bytes db]
  (.put db key-bytes value-bytes))

(defn- delete
  [key-str db]
  (.delete db (base/to-bytes key-str)))

(defprotocol RecoverableId
  "an uniqure identifier,it can be recoved after restart,
   but it does not garantee the identifier is sequential,
   for example the identifier is 10 before crash, might be
   13 after going back"
  (init! [this] "init the id")
  (get! [this] "return the current value of id")
  (inc! [this] "increase the id")
  (clear! [this] "clear and reset the state of id"))



(defrecord RecoverableLongId
    [store-key storage long-id]
  RecoverableId
  (init! [this]
    (let [cur-value (base/bytes->long (find-value-by-key store-key storage))
          new-value (if (nil? cur-value) (long 0)
                        (+ cur-value mutable/flush-recoverable-id-interval))]
      (reset! long-id new-value)
      (write (base/to-bytes store-key) (base/long->bytes new-value) storage)))
  (get! [this]
    @long-id)
  (inc! [this]
    (let [inc-value (swap! long-id inc)]
      (if (= 0 (mod inc-value mutable/flush-recoverable-id-interval))
        (write
         (base/to-bytes store-key)
         (base/long->bytes
          (long inc-value))
         storage))
      inc-value))
  (clear! [this]
    (delete store-key storage)
    (reset! long-id 0)))

(defn init-recoverable-long-id
  "factory of recoverable long id"
  [name storage]
  (let [recoverable (->RecoverableLongId name storage (atom -1))]
    (.init! recoverable)
    recoverable))
