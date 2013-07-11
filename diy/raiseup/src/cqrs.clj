(ns
  ^{:doc "the cqrs basic fun"
    :added "1.0"}
  cqrs
  (:require [raiseup.commandbus :as cb]
            [clojure.core.reducers :as r]))

(defn get-ar
  "retrieve the aggregate root by id"
  [ar-name id]
  (let [events (cb/<-read ar-name id)]
    (r/fold f (r/map (fn [])))))
