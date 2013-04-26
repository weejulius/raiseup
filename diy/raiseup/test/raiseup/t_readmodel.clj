(ns raiseup.t-readmodel
  (:require [midje.sweet :refer :all]
            [raiseup.ontime.readmodel :as rm]))

(fact "put int read model"
      (rm/put-in-readmodel :task 1001 {:a 1})
      (rm/get-readmodel :task 1001) => {:a 1})
