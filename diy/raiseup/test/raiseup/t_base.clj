(ns raiseup.t-base
  (:use raiseup.base
        midje.sweet
        [clojure.string :only (join split)]))



(fact "join string with prefix"
      (join-str ","  "abc,d" [1 3 4]) => "abc,d,1,3,4")

(fact "test join string performance"
      (time (dotimes [n 100000]
              (join-str "-" "abc,d" [1 3 n] ))))

(fact "performance"
      (time (dotimes [n 100000]
              (join "-" [1 3 n] ))))

(fact "int array to bytes"
      (count (int-to-bytes [1001 1003 1004])) => 12)


(fact "bytes to int array"
      (byte-to-int-array (int-to-bytes [1004 1002 1003])) => [1004 1002 1003])
