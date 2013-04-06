(ns raiseup.t-storage
  (:use raiseup.storage)
  (:require  [raiseup.base :as base]
             [midje.sweet :as test]))

(def leveldb (open-leveldb "/tmp/recoverable"))
(def long-id (init-recoverable-long-id "event-id" leveldb))
(clear! long-id)

(test/fact "init recoverabel long id"
           (get long-id) => 0
           (inc! long-id) => 1
           (inc! long-id) => 2
           (inc! long-id) => 3)

(test/fact "flush change to store"
           (println (let [long-id1 (init-recoverable-long-id "event-id" leveldb)]
                      (clear! long-id1)
                      (time (dotimes [n 1000001]
                              (inc! long-id1)))))
           (base/bytes->long (find-value-by-key "event-id" leveldb)) => 1000000)
