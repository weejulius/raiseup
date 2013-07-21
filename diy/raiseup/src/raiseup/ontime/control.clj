(ns raiseup.ontime.control
  (:require [cqrs.protocol :as cqrs]
            [raiseup.cqrsroutes :refer :all]
            [bouncer [core :as b] [validators :as v]]
            [raiseup.ontime.query :as q]
            [common.convert :as convert]))

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
    (println "errors" errors)
    (if (nil? errors)
      (handle params)
      {:errors (vals errors)})))

(defn index-view
  "fetch data for index view"
  [params]
  (let [slots (sort-by :ar-id >
                       (q/find-slots-for-user 1))
        grouped-slots (group-by #(nil? (:start-time %)) slots)]
    {:unplanned-slots (grouped-slots true)
     :planned-slots (grouped-slots false)}))

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
                                       :ar-id [v/required]
                                       :start-time v/required)
                          (fn [params]
                             (send-command
                             {:command :start-task-slot
                              :ar :task-slot
                              :ar-id (convert/->long (:ar-id params))
                              :start-time (:start-time params)}))))

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
