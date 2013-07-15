(ns ^{:doc "level db implementation of cqrs"}
  cqrs.leveldb
  (:require [raiseup.base :as base]
            [clojure.java.io :as io]))

(def opened-leveldb (atom {}))

(def default-leveldb-option {})

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
     (if-let [existing-db (get @opened-leveldb db-dir)]
       existing-db
       (let [opened-new-db (open-new-leveldb
                            (java.io.File. db-dir)
                            (org.iq80.leveldb.Options.))]
         (swap! opened-leveldb
                (fn [dbs]
                  (assoc dbs db-dir opened-new-db)))
         opened-new-db)))
  ([db-dir]
     (open-leveldb db-dir default-leveldb-option)))


(defn destroy-leveldb
  ([file options]
     (.destroy org.fusesource.leveldbjni.JniDBFactory/factory
               (io/file file)
               (org.iq80.leveldb.Options.)))
  ([file]
     (destroy-leveldb file default-leveldb-option)))
