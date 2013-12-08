(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [system :as s]
            [cqrs.protocol :refer [Query]])
  (:import (cqrs.protocol ReadModel)))


(defrecord QueryNote [id author page size]
  Query
  (find-by-id [this]
    (.load-entry
     (:readmodel s/system) :note id))
  (query [this]
    (let [p (or page 1)
          s (or size 20)
          basic-query [:from (* s (dec p))
                       :size s]
          rm (:readmodel s/system)
          result (.do-query
                  rm
                  :note
                  (if-not (nil? author)
                    (concat basic-query [:query {:term {:author author}}])
                    basic-query))]
      (prn "query result " result "for " basic-query)
      result)))
