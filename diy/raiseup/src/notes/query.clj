(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [system :as s]
            [cqrs.protocol :refer [Query]])
  (:import (cqrs.protocol ReadModel)))

(defn- read-model {:tag ReadModel}
  []
  (:readmodel s/system))

(defrecord QueryNote [id author page size]
  Query
  (find-by-id [this]
    (.load-entry
     (read-model) :note id))
  (query [this]
    (let [p (or page 1)
          s (or size 20)
          basic-query [:from (* s (dec p))
                       :size s
                       :sort {:ar-id "asc"}]
          rm read-model
          result (.do-query
                  rm
                  :note
                  (if-not (nil? author)
                    (concat basic-query [:query {:term {:author author}}])
                    basic-query))]
     ;; (prn "query result " result "for " basic-query)
      result)))
