(ns raiseup.t-mutable-data
  (:use midje.sweet)
  (:require [raiseup.mutable-data :refer :all]))

(fact "get cache"
      (get-cache "hello" (fn [] 1)) => 1)
(fact "get read model cache"
      (.getMap (readmodel-cache) "hello111") => {})

(fact "change cache"
      (let [c1 (.getMap (readmodel-cache) "hello11")]
        (.put c1 "k1" "v1")
        (.get (.getMap (readmodel-cache) "hello11") "k1") => "v1"))

