(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [cqrs.protocol :refer [Query] :as p]
            [schema.macros :as sm]
            [schema.core :as s]))

(sm/defrecord QueryNote
              [ar :- s/Keyword
               id :- s/Num
               author :- s/Str
               page :- s/Num
               size :- s/Num]
  Query
  (query [this]
         (if-not (nil? author)
           {:query {:term {:author author}}})))


