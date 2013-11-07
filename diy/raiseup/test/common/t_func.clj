(ns t-func
  (:use [midje.sweet]
        [common.func]))

(fact "put if absence"
  (put-if-absence {} [:a] (fn [] 1)) => {:a 1}
  (put-if-absence {} [:a] 1) => {:a 1}
  (put-if-absence {} [:a :b] 2) => {:a {:b 2}})
