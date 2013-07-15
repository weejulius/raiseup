(ns
  ^{:doc "the cqrs basic fun"
    :added "1.0"}
  cqrs
  (:require [raiseup.commandbus :as cb]
            [raiseup.cqrsroutes :as cr]
            [clojure.core.reducers :as r]))

(defn get-ar
  "retrieve the aggregate root state by replay events for ar"
  ([events get-handler]
     (r/reduce
      (fn [state event]
        ((get-handler (:event event) :domain) state event)) {} events))
  ([ar-name ar-id]
     (get-ar
      (cb/read-ar-events ar-name ar-id)
      cr/get-event-handler)))


(defn send-command
  "send command to bus")


(defn send-event
  "send event to its listener"
  )


