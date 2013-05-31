(ns raiseup.ontime.control
  (:require [raiseup.commandbus :as cb]
            [raiseup.cqrsroutes :refer :all]
            [bouncer [core :as b] [validators :as v]]))

(defn- send-command
  "send command to bus"
  [command]
  (cb/->send command commandroutes eventroutes))


(defn create-task-slot-action
  "create an task slot"
  [req]
  (let [error-message (first (b/validate
                              req
                              :description v/required))]
    (if (nil? error-message)
      (send-command
       {:command :create-task-slot
        :ar :task-slot
        :user-id 1
        :description (:description req)
        :estimation 40})
      {:errors (vals error-message)})))

(defn handle-command
  [params]
  (let [command-type (keyword (:type params))
        data (:data params)]
    ((case command-type
       :create-task-slot create-task-slot-action) data)))
