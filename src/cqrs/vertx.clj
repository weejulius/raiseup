(ns cqrs.vertx
  "vertx bus implementation"
  (:require [cqrs.protocol :as p]
            [common.component :as compt]
            [clojure.test :refer :all]
            [vertx.eventbus :as bus]
            [vertx.embed :as vertx]
            [common.core :as c]
            [common.logging :as log]))

(defrecord VertxBus [useless]
  p/Bus
  (sends [this command]
    (bus/send (str (:command command)) command))
  (publish [this event]
    (bus/publish (str (:event event)) event))
  (reg [this name handle]
    (bus/on-message (str name)
                    (fn [message]
                      (handle message))
                    (fn [error]
                      (log/info "reg handler" name))))
  compt/Lifecycle
  (init [this options]
    (vertx/set-vertx! (vertx/vertx))
    this)
  (start [this options]
    (doall
      (map (fn [handlers]
             (log/info "```init handlers for bus```" handlers)
             ((c/load-sym handlers)))
           (:handlers options)))
    this)
  (stop [this options]
    this)
  (halt [this options]
    this))


(deftest test-sends
  (let [bus (compt/init (VertxBus. nil) nil)
        command {:ar :car :command :start}]
    (p/reg bus "car/start" (fn [message] (println message)))
    (p/sends bus command)))