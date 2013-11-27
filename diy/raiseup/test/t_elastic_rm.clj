(ns t-elastic-rm
  (:use [midje.sweet])
  (:require [cqrs.elastic-rm :as read]
            [common.component :as component]))
(read/init nil "http://127.0.0.1:9200")
(read/start nil
            {:app "note4"
            ; :settings {:index {:number_of_replicas 1}}
             :mappings
             {"a" {:properties {:ar {:type "string" :store "yes"}
                                 :ar-id {:type "integer" :store "yes"}
                                 :first-name {:type "string" :store "yes"}
                                   :last-name {:type "string" :store "yes"}
                                   :ctime {:type "date" :store "yes"}}}}})
(fact "start elastic read model"
  (let [rm (read/->ElasticReadModel "note4")]

    (.put-entry rm
                {:ar :a
                 :ar-id "100"
                 :first-name "hello"
                 :last-name "word"
                 :ctime (java.util.Date.)})
    (.load-entry rm :a "100") => {}))
