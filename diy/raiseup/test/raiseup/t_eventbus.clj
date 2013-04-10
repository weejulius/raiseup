(ns raiseup.t-eventbus
  (:use midje.sweet
        raiseup.eventbus))

(def v (atom 0))

(defn on-task-created
  [event]
  (reset! v 1))

(defn on-task-created1
  [event]
  (reset! v 2))

(def event-map
  {:task-created [[on-task-created1]
                  [on-task-created]]})


(def event-map1
  {:task-created [[on-task-created on-task-created1]]})

(fact "publish event to handlers"
      (->sent {:event :task-created :event-id 1001} event-map)
      @v => 1)


(fact "the listeners process event concurrently"
      (count
       (filter #(= % 2)
               (take 1000
                     (do
                       (repeatedly #(do
                                      (->sent {:event :task-created :event-id 1001} event-map1)
                                      @v))))))=> (roughly 500 200))

(fact "subscribe event"
      (->sub :task-created on-task-created))
