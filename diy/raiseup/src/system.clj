(ns system
  "functions need side effect"
  (:require [env :as env]
            [cqrs.vertx :refer :all]
            [cqrs.core :as cqrs]
            [cqrs.protocol :as p]
            [common.config :as cfg]
            [common.component :as component]
            [common.logging :as log])
  (:import [clojure.lang IPersistentMap]))


(defn register-event-handler
  [event-type f]
  (cqrs/register-event-handler event-type f
                               env/bus
                               (:readmodel env/system)))

(defn publish-event
  [event]
  (cqrs/publish-event env/bus event))

(defn send-command
  [ar command-type fields & {:as options}]
  (cqrs/send-command env/bus (cqrs/gen-command ar command-type fields (:recoverable-ids env/system))))

(defn register-command-handler
  [command-type f]
  (cqrs/register-command-handler command-type f
                                 env/bus
                                 (:snapshot-db env/system)))

(defn fetch
  "fetch result of query"
  [query]
  (if (:id query)
    ((p/find-by-id query) (:readmodel env/system))
    ((p/query query) (:readmodel env/system))))
