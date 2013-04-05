(ns raiseup.ontime.t-eventstore
  (:use midje.sweet
        raiseup.eventstore
        raiseup.storage)
  (:require [raiseup.base :as base]))

(def leveldb (open-leveldb "/tmp/leveldb-test2" {}))
(def eventdb (open-leveldb "/tmp/eventdb2" {}))
(def level-db-root-dir "/tmp/")

(fact "open level db"
      (open-leveldb "/tmp/leveldb" {}) => (complement nil?))

(fact "open level db for aggregate root"
      (open-leveldb-for-ar level-db-root-dir "hello" {}) => (complement nil?))

(fact "store event ids mapped by aggregate root id"
      (store-events-id-mapped-by-ar-id "task" "10001" [1001] leveldb))

(fact "store event id performance"
      (time (dotimes [n 100000]
              (store-events-id-mapped-by-ar-id "task" (str n) [n] leveldb))))

(fact "store events"
      (store-events :task 10001
                    [{:event :task/task-created
                      :ar-id 10001
                      :event-id 1001
                      :name "task 1"}]
                    leveldb
                    eventdb))

(fact "read single event"
      (read-event (base/to-bytes (str 1001)) eventdb)
      => {:event "task/task-created"
          :ar-id 10001
          :event-id 1001
          :name "task 1"})

(fact "read events"
      (read-events :task 10001 leveldb eventdb)
      => [{:event "task/task-created"
           :ar-id 10001
           :event-id 1001
           :name "task 1"}])

(fact "int array to bytes"
      (count (int-to-bytes [1001 1003 1004])) => 12)

(fact "bytes to int array"
      (byte-to-int-array (int-to-bytes [1004 1002 1003])) => [1004 1002 1003])

(fact "write performance"
      (println
           (time
            (dotimes [n 100000]
              (.put leveldb (base/to-bytes (str n)) (base/to-bytes "word")))))
      => nil?)

(fact "get performance"
      (println
           (time
            (dotimes [n 100000]
              (.get leveldb (base/to-bytes (str n))))))
      => nil?)


(.close leveldb)
