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

(defn on-task-created-heavy
  [event]
  (Thread/sleep 1000)
  (reset! v 3))



(def event-map
  {:task-created [[on-task-created1]
                  [on-task-created]]})

(def event-map1
  {:task-created [[on-task-created on-task-created1]]})

(def event-map2
  {:task-created [[on-task-created1]
                  [on-task-created-heavy {:asyn true}]]})

(fact "publish event to handlers"
      (->send {:event :task-created :event-id 1001} event-map)
      @v => 1)


(fact "the listeners process events"
      (count
       (filter #(= % 2)
               (take 100000
                     (do
                       (repeatedly
                        #(do
                           (->send {:event :task-created :event-id 1001} event-map1)
                           @v))))))=> 100000)

