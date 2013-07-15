(ns raiseup.eventbus
  (:require [raiseup.cqrsroutes :as cr]))


(defn ->send
  ^{:doc "send events to their listeners, if the execution
          of event does not take much time, do not use pararellasm"}
  [event event-router]
  (doseq [handler
          (cr/get-event-handler-with-exclusion (:event event) :domain event-router)]
    (handler event)))

(defn ->sub
  ^{:doc "subscribe event to listeners"}
  [event-name listener])
