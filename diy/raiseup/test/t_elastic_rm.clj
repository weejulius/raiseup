(ns t-elastic-rm
  (:use [clojure.test])
  (:require [cqrs.elastic-rm :as read]
            [common.component :as component]
            [clojurewerkz.elastisch.native.index    :as idx]
            [taoensso.timbre.profiling :as p]))

(def app "unittest")
(def rm (read/->ElasticReadModel app))

(defn elastic-fixture [f]
  (.init rm {:host "127.0.0.1"
             :port 9300
             :cluster-name "elasticsearch"
             :app  app
             :mappings
             {"test" {:properties
                      {:ar         {:type "string" :store "yes"}
                       :ar-id      {:type "integer" :store "yes"}
                       :first-name {:type "string" :store "yes"}
                       :last-name  {:type "string" :store "yes"}
                       :ctime      {:type "date" :store "yes"}}}}})
  (f)
  (if (idx/exists? app)
    (idx/delete app)))

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
    (is (= (:first-name (.load-entry rm :test 101)) nil))
    (.put-entry rm
                {:ar :test
                 :ar-id 100
                 :first-name "hello1"
                 :last-name "word"
                 :ctime (java.util.Date.)})
    (is (= (:first-name (.load-entry rm :test 100)) "hello1"))))

(deftest test-query
  (do
    (dotimes [n 4]
      (.put-entry rm
                  {:ar :test
                   :ar-id n
                   :first-name "hello"
                   :last-name "word"
                   :ctime (java.util.Date.)}))
    (idx/refresh app)
    (let [[first-name last-name page size] ["hello" "word" 2 2]
          result (.do-query
                  rm :test
                  [:query {:term {:first-name first-name}}
            ;;       :search_type "query_then_fetch"
                   :from (* page (dec size))
                   :size size])]
      (println (:hits result))
      (is (= 2 (count (-> result :hits :hits)))))))

;; elapsed time 2070 msecs
(deftest test-put-performance
  (p/profile :info :put-perf
             (dotimes [n 100]
               (.put-entry rm
                           {:ar :test
                            :ar-id n
                            :first-name "hello"
                            :last-name "word"
                            :ctime (java.util.Date.)}))))
;;http elapsed time 1200 msecs/100
;;native elapsed time 256 msecs/100
;;native elapsed time 2 s/1000
;;native elapsed time 6 s/3000
(deftest test-query-performance
  (dotimes [n 3000]
    (.put-entry rm
                {:ar :test
                 :ar-id n
                 :first-name "hello"
                 :last-name "word"
                 :ctime (java.util.Date.)}))
  (p/profile :info :query-perf
             (dotimes [n 3000]
               (.do-query
                rm :test
                [:query {:term {:first-name "hello"}}
                 :from n
                 :size 1]))))

(run-tests)
