(ns ^{:doc "the queries for notes"}
  notes.query
  (:require [schema.macros :as sm]
            [schema.core :as s]
            [cqrs.core :as cqrs]))

(cqrs/def-query
  :note

  {
    :schema
    {(s/optional-key :author) s/Str
     (s/optional-key :fields) [s/Keyword]}

    :query
    (fn [p]
      (let [q (if-not (nil? (:author p))
                {:query {:term {:author (:author p)}}}
                {})]
        (if-not (nil? (:fields p))
          (merge q {:fileds (:fields p)})
          q)))})



(cqrs/def-query
  :user
  {
    :schema
    {(s/optional-key :name) s/Str}


    :query
    (fn [p]
      (if-not (nil? (:name p))
        {:query {:term {:name (:name p)}}}))})

