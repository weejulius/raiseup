(ns cqrs.t-protocol
  (:use [midje.sweet]
        [cqrs.protocol]))

(fact "generate event"
  (:start-time (gen-event :game-started :fruit 1 {:start-time "now"})) => "now")
