(ns raiseup.ontime.t-commandhandler
  (:use midje.sweet
        raiseup.ontime.commandhandler))

(def leveldb (open-leveldb "/tmp/leveldb-test6" {}))
(def level-db-root-dir "/tmp/")

(fact "open level db"
      (open-leveldb "/tmp/leveldb" {}) => (complement nil?))

(fact "open level db for aggregate root"
      (open-leveldb-for-ar level-db-root-dir "hello" {}) => (complement nil?))


(fact "store event ids mapped by aggregate root id"
      (store-events-id-mapped-by-ar-id :task3 10001 [10001] leveldb))

(fact "store event id performance"
      (time (dotimes [n 100000]
              (store-events-id-mapped-by-ar-id :task3 n [n] leveldb))))

(fact "write performance"
      (println
           (time
            (dotimes [n 100000]
              (.put leveldb (to-bytes (str n) "UTF-8") (to-bytes "word" "UTF-8")))))
      => nil?)

(fact "get performance"
      (println
           (time
            (dotimes [n 100000]
              (.get leveldb (to-bytes (str n) "UTF-8")))))
      => nil?)

(.close leveldb)
