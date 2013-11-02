(ns raiseup.ontime.control
  (:require [cqrs.protocol :as cqrs]
            [raiseup.ontime.commands :refer :all]
            [raiseup.cqrsroutes :refer :all]
            [bouncer [core :as b] [validators :as v]]
            [raiseup.ontime.query :as q]
            [common.convert :as convert]))


(defn- send-command
  "send command to bus"
  [command]
  (cqrs/send-command
   command
   #(get-event-handler-with-exclusion (:event %)
                                      :domain
                                      event-routes)))

(defn index-view
  "fetch data for index view"
  [params]
  (let [slots (sort-by :ar-id >
                       (q/find-slots-for-user 1))
        grouped-slots (group-by #(nil? (:start-time %)) slots)]
    {:unplanned-slots (grouped-slots true)
     :planned-slots (grouped-slots false)
     :date-fmt #(convert/->str (convert/->date (convert/->long %)))}))

(defn create-task-slot-action
  "create an task slot"
  [params]
  (send-command
   (->CreateTaskSlot :task-slot nil 1 (:description params) nil 40)))

(defn delete-task-slot-action
  [req]
  (send-command
   (->DeleteTaskSlot :task-slot (:ar-id req) 1)))

(defn start-task-slot-action
  "action to start task slot"
  [params]
  (send-command
   (->StartTaskSlot
    :task-slot
    (convert/->long (:ar-id params))
    (:start-time params))))

(defn handle-request
  "handle the http request"
  [request]
  (let [request-type (keyword (:type request))
        request-params (:data request)]
    ((case request-type
       :create-task-slot create-task-slot-action
       :delete-task-slot delete-task-slot-action
       :start-task-slot start-task-slot-action) request-params)))
