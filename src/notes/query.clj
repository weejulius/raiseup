(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [schema.macros :as sm]
            [schema.core :as s]
            [cqrs.core :as cqrs]))

(cqrs/def-query
  :note

  {
    :schema
    {(s/optional-key :author) s/Str}

    :query
    (fn [p]
      (if-not (nil? (:author p))
        {:query {:term {:author (:author p)}}}))})



(cqrs/def-query
  :user
  {
    :schema
    {(s/optional-key :name) s/Str}


    :query
    (fn [p]
      (if-not (nil? (:name p))
        {:query {:term {:name (:name p)}}}))})

