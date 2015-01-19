(ns zjy.command
  (:require
    [cqrs.core :as cqrs]
    [schema.core :as s]
    [common.core :as c]))




(cqrs/def-command-schema :active-account
  {:game-name  s/Str
   :game-server s/Str
   :dirty-words (s/enum :hate :no-matter :accept);;是否允许脏话
   :age (s/enum :little :yung :adult) ;;年龄范围
   :password (s/both s/Str
                     (s/pred #(c/between (count %) 8 16)))
   :hashed-password s/Str
   :ctime   s/Num})
