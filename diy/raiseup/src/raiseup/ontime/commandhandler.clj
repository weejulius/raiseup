(ns raiseup.ontime.commandhandler
  (:require [clojure.data.json :as json]
            [raiseup.base :as util]))


(defn transact
  ^{:added "0.1"
    :abbre "fun=>function"}
  [db fun]
   (try (fun db)
         (finally (.close db))))




(defn process-kv
  ^{:added "1.0"
    :abbre "kv => a pair of key value)"
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

(defn get-value-from-leveldb
  [key-str db]
  (let [value-byte (.get db (to-bytes key-str "UTF-8"))]
    (if (nil? value-byte) nil
        (new java.lang.String value-byte "UTF-8"))))

(defn store-events-id-mapped-by-ar-id
  [ar-name-str ar-id-str event-ids db]
  (let [batch-process (.createWriteBatch db)
        charset "UTF-8"
        store-key (str ar-name-str ar-id-str)
        current-eventids (get-value-from-leveldb ar-id-str db)
        appended-events (util/join-str "," current-eventids event-ids )]
    (.put batch-process (to-bytes ar-id-str charset) (to-bytes appended-events charset))
    (.write db batch-process)))

(defn write-events-to-leveldb
  [events events-db]
  (let [batch  (.createWriteBatch events-db)]
    (println events)
    (doseq [event events]
      (.put batch
            (to-bytes (str (:event-id event)) "UTF-8")
            (to-bytes (json/write-str event) "UTF-8")))
    (.write events-db batch)))


(defn store-events
  ^{:added "1.0"
    :abbre "ar->aggregate root"
    :doc "store the events of aggregate root"}
  [ar-name ar-id events ar-eventids-db events-db]
  (let [ar-name-str (name ar-name)
        ar-id-str (str ar-id)
        event-ids (map #(:event-id %) events)]
    (store-events-id-mapped-by-ar-id ar-name-str ar-id-str event-ids ar-eventids-db)
    (write-events-to-leveldb events events-db)))

(defn read-event-ids
  [ar-id-str ar-event-ids-db]
  ((.get (to-bytes ar-id-str "UTF-8") ar-event-ids-db)))

(defn read-event
  [event-id-byte events-db]
  (let [event-byte (.get events-db event-id-byte)]
    (println event-byte)
    (if (nil? event-byte)
      nil
      (json/read-str (String. event-byte "UTF-8")))))

(defn read-events
  [ar-name ar-id ar-event-ids-db events-db]
  (let [ar-id-str (str ar-id)
        event-ids-byte (read-event-ids ar-id-str ar-event-ids-db)]
    (print event-ids-byte)
    (mapcat
      
      event-ids-byte)))


