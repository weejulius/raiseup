(ns common.t-strs
  (:use common.strs
        midje.sweet
        [clojure.string :only (join split)])
  (:require [clj-time.core :as cc]))

(fact "join string with prefix"
      (join-str ","  "abc,d" [1 3 4]) => "abc,d,1,3,4")

(fact "test join string performance"
      (time (dotimes [n 100000]
              (join-str "-" "abc,d" [1 3 n] ))))

(fact "performance"
  (time (dotimes [n 100000]
          (join "-" [1 3 n] ))))
