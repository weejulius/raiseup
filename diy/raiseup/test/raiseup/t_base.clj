(ns raiseup.t-base
  (:use raiseup.base
        midje.sweet
        [clojure.string :only (join split)]))

(fact "join string"
      (join-str [1 3 4] ",") => "1,3,4")

(fact "performance"
      (time (dotimes [n 100000]
                    (join-str [1 3 n] "-"))))

(fact "performance"
      (time (dotimes [n 100000]
                    (join "-" [1 3 n] ))))
