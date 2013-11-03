(ns raiseup.mutable-data
  (:require [common.cache :as c])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))

(defn readmodel-cache
  []
  (c/get-cache :readmodel-cache (fn [] (Hazelcast/newHazelcastInstance nil))))
