(ns raiseup.t-eventbus
  (:use midje.sweet))

(defn on-task-created
  [event]
  (println event))

(fact "publish event to handler"
      (->sent {:event :task-created :event-id 1001}))

(fact "subscribe event"
      (->sub :task-created on-task-created))
