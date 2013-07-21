(ns raiseup.ontime.control
  (:require [cqrs.protocol :as cqrs]
            [raiseup.cqrsroutes :refer :all]
            [bouncer [core :as b] [validators :as v]]))

(defn- send-command
  "send command to bus"
  [command]
  (cqrs/send-command command
                     command-routes
                     #(get-event-handler-with-exclusion (:event %)
                                                        :domain
                                                        event-routes)))

(defn- handle-with-validation
  "validate the params before handlers"
  [params validate-fn handle]
  (let [errors (first (validate-fn params))]
    (if (nil? errors)
      (handle params)
      {:errors (vals errors)})))

(defn create-task-slot-action
  "create an task slot"
  [params]
  (handle-with-validation  params
                           #(b/validate % :description v/required)
                           #(send-command
                             {:command :create-task-slot
                              :ar :task-slot
                              :user-id 1
                              :description (:description %)
                              :estimation 40})))

(defn delete-task-slot-action
  [req]
  (send-command
   {:command :delete-task-slot
    :ar :task-slot
    :user-id 1
    :ar-id (:ar-id req)}))

(defn start-task-slot-action
  "action to start task slot"
  [params]
  (handle-with-validation params
                          #(b/validate %
                                       :ar-id [v/required v/number]
                                       :start-time v/required)
                          #(send-command
                            {:command :start-task-slot
                             :ar :task-slot
                             :ar-id (:ar-id %)
                             :start-time (:start-time %)})))

(defn handle-command
  "handle the http request"
  [request]
  (let [command-type (keyword (:type request))
        command-params (:data request)]
    (println "command type:" command-type "params:" command-params)
    ((case command-type
       :create-task-slot create-task-slot-action
       :delete-task-slot delete-task-slot-action
       :start-task-slot start-task-slot-action) command-params)))
