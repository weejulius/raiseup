(ns raiseup.t-eventstore
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
              (store-events-id-mapped-by-ar-id "task1" (str n) [n] leveldb))))

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

(fact "write performance"
      (println
           (time
            (dotimes [n 20000]
              (store-events :task2 n
                    [{:event :task/task-created
                      :ar-id n
                      :event-id n
                      :name "task 1"}]
                    leveldb
                    eventdb))))
      => nil?)

(fact "get performance"
      (println
           (time
            (dotimes [n 100000]
              (read-events :task2 n leveldb eventdb))))
      => nil?)


(.close leveldb)
