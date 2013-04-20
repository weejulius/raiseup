(ns raiseup.t-commandbus
  (:require [midje.sweet :refer :all]
            [raiseup.commandbus :refer :all]))

(defn- create-task
  [command]
  {:name (:name command)
   :event :event-created
   :ar (:ar command)
   :ar-id (:ar-id command)})

(defn- task-created
  [event]
  (println event))

(def command-router {:create-task create-task})

(def event-router {:task-created [[task-created]]})

(fact
 (->send
  {:command :create-task :name "hello" :ar :task :ar-id 1001}
  command-router event-router)
 => 1)

(fact (<-read :task 1001) => 1)
