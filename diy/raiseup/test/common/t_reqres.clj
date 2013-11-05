(ns common.t-reqres
  (:require [midje.sweet :refer :all]
            [common.reqres :as req]))

(fact "convert ds to params recognied by template engine"
      (req/->template-param {:hello-word "yes"})=> {"hello_word" "yes"})
