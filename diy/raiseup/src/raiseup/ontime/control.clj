(ns raiseup.ontime.control
  (:require [cqrs.protocol :as cqrs]
            [raiseup.cqrsroutes :refer :all]
            [bouncer [core :as b] [validators :as v]]))

(defn- send-command
  "send command to bus"
  [command]
  (cqrs/send-command command
                     command-routes
                     #(get-event-handler-with-exclusion
                       (:event %)
                       :domain
                       event-routes)))


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

(defn delete-task-slot-action
  [req]
  (send-command
   {:command :delete-task-slot
    :ar :task-slot
    :user-id 1
    :ar-id (:ar-id req)}))

(defn handle-command
  [params]
  (let [command-type (keyword (:type params))
        data (:data params)]
    (println "command type:" command-type "params:" params)
    ((case command-type
       :create-task-slot create-task-slot-action
       :delete-task-slot delete-task-slot-action) data)))
