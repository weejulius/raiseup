(ns raiseup.t-storage
  (:use midje.sweet)
  (:require  [raiseup.base :as base]
             [raiseup.storage :as store]))

(store/destroy-leveldb "/tmp/recoverable1")
(def leveldb (store/open-leveldb "/tmp/recoverable1"))
(def long-id (store/init-recoverable-long-id "event-id" leveldb))
(.clear! long-id)

(fact "init recoverabel long id"
      (.get! long-id) => 0
      (.inc! long-id) => 1
      (.inc! long-id) => 2
      (.inc! long-id) => 3)

(fact "flush change to store"
      (println (let [long-id1 (store/init-recoverable-long-id "event-id" leveldb)]
                 (.clear! long-id1)
                 (time (dotimes [n 1000001]
                         (.inc! long-id1)))))
      (base/bytes->long (store/find-value-by-key "event-id" leveldb)) => 1000000)

(.close leveldb)
