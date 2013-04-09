(ns raiseup.t-eventstore
  (:use midje.sweet
        raiseup.eventstore)
  (:require [raiseup.base :as base]
            [raiseup.storage :as store]))

(store/destroy-leveldb "/tmp/leveldb-test1")
(store/destroy-leveldb "/tmp/eventdb1")

(def leveldb (store/open-leveldb "/tmp/leveldb-test1" {}))
(def eventdb (store/open-leveldb "/tmp/eventdb1" {}))


(fact "open level db"
      (.getName (class leveldb))
      =>"org.fusesource.leveldbjni.internal.JniDB")

(fact "store event ids mapped by aggregate root id"
      (store-events-id-mapped-by-ar-id "task" "10001" [1001] leveldb))

(fact "store event id performance"
      (time (dotimes [n 100000]
              (store-events-id-mapped-by-ar-id "task1" (str n) [n] leveldb))))

(fact "store events"
      (store-events :task5 10001
                    [{:event :task/task-created
                      :ar-id 10001
                      :event-id 100000002
                      :name "task 1"}]
                    leveldb
                    eventdb))

(fact "read single event"
      (read-event (base/int-to-bytes [100000002]) eventdb)
      => {:event :task/task-created
          :ar-id 10001
          :event-id 100000002
          :name "task 1"})

(fact "read events"
      (read-events :task5 10001 leveldb eventdb)
      => [{:event :task/task-created
           :ar-id 10001
           :event-id 100000002
           :name "task 1"}])

(fact "write performance"
      (time
            (dotimes [n 200000]
              (store-events :task2 n
                    [{:event :task/task-created
                      :ar-id n
                      :event-id n
                      :name "task 1"}]
                    leveldb
                    eventdb)))
      => nil?)

(fact "get performance"
      (println
           (time
            (dotimes [n 100000]
              (read-events :task2 n leveldb eventdb))))
      => nil?)


(.close leveldb)
(.close eventdb)
