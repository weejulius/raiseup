(ns raiseup.t-views
  (:require [midje.sweet :refer :all]
            [raiseup.ontime.views :as v]))

(fact "put int view"
      (v/put-in-view :task 1001 {:a 1})
      (v/get-view :task 1001) => {:a 1})
