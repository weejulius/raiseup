(ns ^{:doc "evaludate the performance of the app"}
  t-evaludate-perf
  (:require [main :as m]
            [system :as s]
            [common.config :as cfg]
            [common.component :as component]
            [clojurewerkz.elastisch.rest.index :as idx]
            [notes.commands :refer :all])
  (:use  [ontime.handler]
         [clojure.test]))

(defn- setup-server
  [f]
  (do
    (alter-var-root #'s/system component/stop {})
    (alter-var-root #'s/system component/init
                    {:id-db-path       (cfg/ret :es :id-db-path)
                     :leveldb-option   (cfg/ret :leveldb-option)
                     :snapshot-db-path (cfg/ret :es :snapshot-db-path)
                     :events-db-path   (cfg/ret :es :events-db-path)
                     :elastic          (assoc (cfg/ret :elastic)
                                                  :app "test-perf")})
    (alter-var-root #'s/system component/start {:port "8080"
                                                :host "localhost"
                                                :routes  #'app-routes})
    (f)
    (alter-var-root #'s/system component/stop {})
    (idx/delete "test-perf")))

(use-fixtures :once setup-server)

(deftest evaluate-new-note
  (time
   (do
     (dotimes [n 100]
       (s/send-command
        (->CreateNote :note
                      "i am auth" "i am test" "i am content" (java.util.Date.))))
     (Thread/sleep 3000))))

(run-tests)
