(ns common.t-convert
  (:use common.convert
        midje.sweet)
  (:require [clj-time.core :as cc]))

(fact "int array to bytes"
      (count (->bytes [1001 1003 1004])) => 12)


(fact "bytes to int array"
      (->data (->bytes [1004 1002 1003])) => [1004 1002 1003])

(fact "bytes to data"
      (->data (->bytes [1 2 3]))=> [1 2 3])

(fact "bytes to long"
      (->long (->bytes 10)) => 10)

(fact "string to long"
      (->long "1") => 1)

(fact "string to map"
      (->map "{\"hello\":\"word\"}")=> {:hello "word"})

(fact "map to str"
      (->str {:hello "word"}) => "{\"hello\":\"word\"}")

(fact "date to str"
      (->str (.toDate (cc/date-time 2013 06 21))) => "2013-06-21")
