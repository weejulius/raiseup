(ns cqrs.mapdb
  (:require [cqrs.storage :as s]
            [common.component :as c])
  (:import (org.mapdb DB DBMaker)
           (java.util Map)
           (java.io File)))


(defn- create-or-open-file
  [^String path]
  (let [file (File. path)]
    (if-not (.exists file)
      (.createNewFile file))
    file))

(defrecord MapdbStore
           [^DB db ^Map map]
  s/Store
  (ret-value [this key]
    (.get map key))
  (write [this key value]
    (.put map key value)
    (.commit db))
  (write-in-batch [this items]
    (doseq [[k v] items]
      (.put map k v))
    (.commit db))
  (delete [this key]
    (.remove map key)
    (.commit db))
  (map [this f]
    (clojure.core/map #(apply f %) (iterator-seq map)))
  (close [this]
    (.close db))

  c/Lifecycle
  (init [this options]
    this)
  (start [this options]
    (let [^DB db (.make (doto (DBMaker/newFileDB (create-or-open-file (:path options)))
                          .closeOnJvmShutdown))]
      (-> this
          (assoc :db db)
          (assoc :map (.getHashMap db ^String (:name options))))))
  (stop [this options]
    (s/close this)
    this)
  (halt [this options]
    this))


;;performance comparation between leveldb and mapdb

;; leveldb has a better write whereas mapdb has a better read rate

#_(let [db (c/start
           (s/->LeveldbStore nil)
           {:path "/tmp/leveldbtest"})]
  (println (time (dotimes [n 1000000]
                   (s/write db n {:name :test :ctime 1231232131 :id n}))))
  (println (time (dotimes [n 1000000]
                   (s/ret-value db n)
                   nil)))
  (s/close db))



#_(let [db (c/start
           (->MapdbStore nil nil)
           {:name "hello" :path "/tmp/mapdbtest1"})]
  (println (time (dotimes [n 1000]
                   (s/write db n {:name :test :ctime 1231232131 :id n}))))
  (println (time (dotimes [n 1000]
                   (s/ret-value db (- 1000 n))
                   nil)))
  (s/close db))

