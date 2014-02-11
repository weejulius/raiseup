(ns notes.commands
  (:require
    [cqrs.core :as cqrs]
    [schema.core :as s]))

(cqrs/def-schema :create-note
                 {:author  (s/maybe s/Str)
                  :title   (s/both s/Str
                                   (s/pred #(> 100 (count %))))
                  :content (s/both s/Str
                                   (s/pred #(> 5000 (count %))))
                  :ctime   s/Num})


(cqrs/def-schema :update-note
                 {:ar-id   s/Num
                  :title   (s/maybe (s/both s/Str
                                            (s/pred #(> 100 (count %)))))
                  :content (s/maybe (s/both s/Str
                                            (s/pred #(> 5000 (count %)))))
                  :utime   s/Num})

(cqrs/def-schema :delete-note
                 {:ar-id s/Num})


