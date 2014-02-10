(ns common.t-cache
  (:use [midje.sweet])
  (:require [common.cache :as c]))

(fact "get nested cache"
  (reset! c/caches {})
  (c/get-cache :key [:d] (fn [] nil)) => nil
  (c/get-cache :key [:d] (fn [] 1)) => 1)
