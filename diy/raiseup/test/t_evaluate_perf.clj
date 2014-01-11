(ns ^{:doc "evaludate the performance of the app"}
  t-evaludate-perf
  (:require [main :as m]
            [system :as s]
            [common.config :as cfg]
            [common.component :as component]
            [clojurewerkz.elastisch.rest :as es]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
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
    (try (f)
         (catch Exception e
           e))
    (alter-var-root #'s/system component/stop {})
    (idx/delete "test-perf")))

(use-fixtures :once setup-server)

(defn- is-finished
  [n wait-time max-times]
  (do
    (prn "starting to wait" (java.util.Date.))
    (loop [times 0]
      (let [result (doc/count "test-perf" "note")
            total (esrsp/count-from result)]
        (when (and (not (= n total)) (< times max-times))
          (println "current size" total)
          (Thread/sleep wait-time)
          (recur (inc times)))))
    (prn "finished" (java.util.Date.))))

(deftest evaluate-new-note
  (is (= 0 (esrsp/count-from
            (doc/count "test-perf" "note"))))
  (time
   (do
     (dotimes [n 800]
       (s/send-command
        (->CreateNote :note
                      "i am auth" "i am test" "i am content" (java.util.Date.))))
     (is-finished 800 1500 10))))

(run-tests)
