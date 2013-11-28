(ns t-elastic-rm
  (:use [clojure.test])
  (:require [cqrs.elastic-rm :as read]
            [common.component :as component]
             [clojurewerkz.elastisch.rest.index    :as idx]))

(def app "unittest")
(def rm (read/->ElasticReadModel app))

(defn elastic-fixture [f]
  (.init rm {:url "http://127.0.0.1:9200"
             :app  app
             :settings {:index {:number_of_replicas 1}}
             :mappings
             {"test" {:properties {:ar {:type "string" :store "yes"}
                                :ar-id {:type "integer" :store "yes"}
                                :first-name {:type "string" :store "yes"}
                                :last-name {:type "string" :store "yes"}
                                :ctime {:type "date" :store "yes"}}}}})
  (f)
  (idx/delete app))

(use-fixtures :each elastic-fixture)

(deftest  test-put-and-load
  (let []
    (.put-entry rm
                {:ar :test
                 :ar-id 100
                 :first-name "hello"
                 :last-name "word"
                 :ctime (java.util.Date.)})
    (is (= (:first-name (.load-entry rm :test 100)) "hello"))
    (is (= (:last-name (.load-entry rm :test 100)) "word"))
    (is (= (:first-name (.load-entry rm :test 101)) nil))))

(deftest test-query
  (dotimes [n 4]
    (.put-entry rm
                {:ar :test
                 :ar-id n
                 :first-name "hello"
                 :last-name "word"
                 :ctime (java.util.Date.)}))
  (let [[first-name last-name page size] ["hello" "word" 2 2]
        result (.do-query
                rm :test
                [:query {:term {:first-name first-name
                                :last-name last-name}}
                 :search_type "query_then_fetch"
                 :from (inc (* page (dec size)))
                 :size size])]
    (println result)
    (is (= 2 (count result)))))
(run-tests)
