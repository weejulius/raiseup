(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [cqrs.protocol :refer [Query] :as p]
            [env :as env]))

(defn- read-model
  []
  (:readmodel env/system))

(defrecord QueryNote [id author page size]
  Query
  (find-by-id [this]
    (fn [readmodel]
      (p/load-entry
        readmodel :note id)))
  (query [this]
    (let [p (or page 1)
          s (or size 20)
          basic-query [:from (* s (dec p))
                       :size s
                       :sort {:ar-id "asc"}]]
      (fn [readmodel]
        (p/do-query
          readmodel
          :note
          (if-not (nil? author)
            (concat basic-query [:query {:term {:author author}}])
            basic-query))))))
