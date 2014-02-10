(ns ^{:doc "level db implementation of cqrs"}
  cqrs.leveldb
  (:require [clojure.java.io :as io]
            [common.config :as cfg]
            [common.logging :as log])
  (:import (org.iq80.leveldb Options)
           (org.fusesource.leveldbjni JniDBFactory)
           (java.io File)))

(defn- open-new-leveldb
  "before using the leveldb open it"
  [file ^Options leveldb-options]
  (let [factory (JniDBFactory/factory)]
    (try
      (.compressionType leveldb-options org.iq80.leveldb.CompressionType/NONE)
      (.open
        factory
        file
        leveldb-options)
      (catch Exception e
        (do
          (log/error "the db is not able be opened by " e ",let us repair and retry")
          (.repair
            factory
            file
            leveldb-options)
          (.open
            factory
            file
            leveldb-options))))))

(defn open-leveldb!
  ^{:added       "1.0"
    :doc         "open the level db to be used,
                  the level db object is cached once it is opened"
    :side-affect "true"}
  [^String db-dir options]
  (open-new-leveldb
    (File. db-dir)
    (Options.)))


