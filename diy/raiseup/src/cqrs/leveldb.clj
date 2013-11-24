(ns ^{:doc "level db implementation of cqrs"}
  cqrs.leveldb
  (:require [clojure.java.io :as io]
            [common.config :as cfg]
            [common.logging :as log]))

(defn- open-new-leveldb
  "before using the leveldb open it"
  [file leveldb-options]
  (try
    (.compressionType leveldb-options org.iq80.leveldb.CompressionType/NONE)
    (.open
     (org.fusesource.leveldbjni.JniDBFactory/factory)
     file
     leveldb-options)
    (catch Exception e
      (do
        (log/error "the db is not able be opened by " e ",let us repair and retry")
        (.repair
         (org.fusesource.leveldbjni.JniDBFactory/factory)
         file
         leveldb-options)
        (.open
         (org.fusesource.leveldbjni.JniDBFactory/factory)
         file
         leveldb-options)))))

(defn open-leveldb!
  ^{:added "1.0"
    :doc   "initialize or open the level db to be used,
            the level db object is cached once it is opened"
    :side-affect "true"}
  ([opened-dbs db-dir options]
     (if-let [existing-db (get @opened-dbs db-dir)]
       existing-db
       (let [opened-new-db (open-new-leveldb
                            (java.io.File. db-dir)
                            (org.iq80.leveldb.Options.))]
         (swap! opened-dbs
                (fn [dbs]
                  (assoc dbs db-dir opened-new-db)))
         opened-new-db)))
  ([opened-dbs db-dir]
     (open-leveldb! opened-dbs db-dir (cfg/ret :leveldb-option))))
