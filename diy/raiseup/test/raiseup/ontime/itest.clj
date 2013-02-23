(ns raiseup.ontime.itest
  (:use midje.sweet))

(fact
 (+ 1 1) => 2
 (+ 1 2)=> 3
 (+ 1 3)=> 4
 (+ 2 4) => 6)
