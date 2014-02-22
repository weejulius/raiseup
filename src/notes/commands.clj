(ns notes.commands
  (:require
    [cqrs.core :as cqrs]
    [schema.core :as s]
    [common.core :as c]))

(cqrs/def-schema :create-note
                 {:author  (s/maybe s/Str)
                  :title   (s/both s/Str
                                   (s/pred #(c/between (count %) 2 100)))
                  :content (s/both s/Str
                                   (s/pred #(c/between (count %) 2 5000)))
                  :ctime   s/Num})


(cqrs/def-schema :update-note
                 {:ar-id                  s/Num
                  (s/optional-key :title) (s/maybe (s/both s/Str
                                                           (s/pred #(c/between (count %) 2 100))))
                  :content                (s/maybe (s/both s/Str
                                                           (s/pred #(c/between (count %) 5 5000))))
                  :utime                  s/Num})

(cqrs/def-schema :delete-note
                 {:ar-id s/Num})


(cqrs/def-schema :create-user
                 {:name            (s/both s/Str
                                           (s/pred #(c/between (count %) 5 16)))
                  :password        (s/both s/Str
                                           (s/pred #(c/between (count %) 5 16)))
                  :hashed-password s/Str
                  :ctime           s/Num})


(cqrs/def-schema :login-user
                 {:ar-id      s/Num
                  :login-time s/Num})


(cqrs/def-schema :logout-user
                 {:ar-id       s/Num
                  :logout-time s/Num})