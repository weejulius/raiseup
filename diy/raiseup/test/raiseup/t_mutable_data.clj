(ns raiseup.t-mutable-data
  (:use midje.sweet)
  (:require [raiseup.mutable-data :refer :all]))

(fact "get cache"
      (get-cache "hello" (fn [] 1)) => 1)
(fact "get view cache"
      (.getMap (view-cache) "hello111") => {})

(fact "change cache"
      (let [c1 (.getMap (view-cache) "hello11")]
        (.put c1 "k1" "v1")
        (.get (.getMap (view-cache) "hello11") "k1") => "v1"))

