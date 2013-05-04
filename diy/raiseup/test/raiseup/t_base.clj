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

(fact "bytes to data"
      (bytes->data (data->bytes [1 2 3]))=> [1 2 3])

(fact "bytes to long"
      (->long (long->bytes 10)) => 10)

(fact "string to long"
      (->long "1") => 1)

(fact "string to map"
      (->map "{\"hello\":\"word\"}")=> {"hello" "word"})
