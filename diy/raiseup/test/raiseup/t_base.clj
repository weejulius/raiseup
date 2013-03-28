(ns raiseup.t-base
  (:use raiseup.base
        midje.sweet))

(fact "join string"
      (join-str "abc,dc,abc" "," [1 3 4]) => "abc,dc,abc,1,3,4"
      (join-str "," [1 3 4]) => "1,3,4")
