(ns system
  "functions need side effect"
  (:require [cqrs.vertx :refer :all]
            [cqrs.core :as cqrs]
            [cqrs.protocol :as p]
            [common.config :as cfg]
            [common.components :as component]
            [common.logging :as log])
  (:import [clojure.lang IPersistentMap]))


(defn register-event-handler
  [event-type f]
  (cqrs/register-event-handler event-type f
                               (:bus component/state)
                               (:readmodel component/state)))

(defn publish-event
  [event]
  (cqrs/publish-event (:bus component/state) event))

(defn send-command
  ([ar command-type fields & {:as options}]
   (cqrs/send-command (:bus component/state)
                      (cqrs/gen-command ar command-type fields
                                        (:recoverable-ids component/state)
                                        (:snapshot-db component/state))))
  ([cmd]
   (cqrs/send-command (:bus component/state)
                      (cqrs/gen-command cmd
                                        (:recoverable-ids component/state)
                                        (:snapshot-db component/state)))))

(defn register-command-handler
  [command-type f]
  (cqrs/register-command-handler command-type f
                                 (fn [] (:bus component/state))
                                 (fn [] (:snapshot-db component/state))))

(schema.macros/defn fetch
  "fetch result of query"
  [query :- p/Query]
  (cqrs/fetch (:readmodel component/state) query))

(schema.macros/defn fetch-first
  "fetch result of query"
  [query :- p/Query]
  (first (fetch query)))