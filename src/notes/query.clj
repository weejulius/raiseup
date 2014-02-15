(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [cqrs.protocol :refer [Query] :as p]
            [schema.macros :as sm]
            [schema.core :as s]))

(sm/defrecord QueryNote
              [ar :- s/Keyword
               id :- (s/maybe s/Num)
               author :- (s/maybe s/Str)
               page :- (s/maybe s/Num)
               size :- (s/maybe s/Num)]
  Query
  (query [this]
         (if-not (nil? author)
           {:query {:term {:author author}}})))


(sm/defrecord QueryUser
              [ar :- s/Keyword
               id :- (s/maybe s/Num)
               name :- (s/maybe s/Str)
               page :- (s/maybe s/Num)
               size :- (s/maybe s/Num)]
  Query
  (query [this]
         (if-not (nil? name)
           {:query {:term {:name name}}})))