(ns cqrs.vertx
  "vertx bus implementation"
  (:require [cqrs.protocol :as p]
            [common.component :as compt]
            [clojure.test :refer :all]
            [vertx.eventbus :as bus]
            [vertx.embed :as vertx]
            [common.logging :as log]))

(defrecord VertxBus []
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
                      (println "reg command handler" name))))
  compt/Lifecycle
  (init [this options]
    (print "starting vert.x")
    (vertx/set-vertx! (vertx/vertx))
    (println "... over")
    this))


(deftest test-sends
  (let [bus (compt/init (VertxBus.) nil)
        command {:ar :car :command :start}]
    (p/reg bus "car/start" (fn [message] (println message)))
    (p/sends bus command)))