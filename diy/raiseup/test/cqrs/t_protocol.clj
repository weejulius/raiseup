(ns cqrs.t-protocol
  (:use [midje.sweet]
        [cqrs.core]))

(fact "generate event"
  (:start-time
   (gen-event :game-started
              {:ar :f :ar-id 1 :fruit 1 :start-time "now"}
              [:start-time])) => "now")
