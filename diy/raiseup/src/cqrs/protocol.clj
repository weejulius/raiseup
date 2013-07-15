(defn bs-demo [coll coll1]
  (if (nil? (next coll))
    coll
    (let [l (reduce
               #(if(< (second %1) %2)
                  (assoc-in (update-in %1 [0] conj (second %1)) [1] %2)
                  (update-in %1 [0] conj %2))
               [[] (first coll)]
               (next coll))]
      (println coll coll1 "=>" (first l) (cons (second l) coll1))
      (recur (first l) (cons (second l) coll1)))))

 (bs-demo [1 6 5 0 3 7 4 10 3 1] [])
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

