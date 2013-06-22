(ns raiseup.t-readmodel
  (:require [midje.sweet :refer :all]
            [raiseup.ontime.readmodel :as rm]))

(fact "put int read model"
      (rm/put-in-readmodel :task 1001 {:a 1})
      (rm/get-readmodel :task 1001) => {:a 1})

(fact "remove from read model"
      (rm/put-in-readmodel :task 1 {:b 2})
      (rm/remove-from-readmodel :task 1)
      (rm/get-readmodel :task 1) => nil?)


(fact "update in read model"
      (rm/put-in-readmodel :task 2 {:b #{1}})
      (rm/update-in-readmodel :task 2 #(update-in % [:b] disj 1))
      (:b (rm/get-readmodel :task 2)) => #{})
