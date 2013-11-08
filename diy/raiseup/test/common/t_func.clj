(ns t-func
  (:use [midje.sweet]
        [common.func]))

(fact "put if absence for immutable map"
  (put-if-absence {} [:a] (fn [] (do (println "running ") 1))) => {:a 1})

(fact "put if absence"
  (put-if-absence! (atom {}) [:a] (fn [] (do (println "running ") 1))) => {:a 1}
  (put-if-absence! (atom {}) [:a] 1) => {:a 1}
  (put-if-absence! (atom {}) [:a :b] 2) => {:a {:b 2}})
