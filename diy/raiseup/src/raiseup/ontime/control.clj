(ns raiseup.ontime.control
  (:require [cqrs.core :as cqrs]
            [raiseup.ontime.commands :refer :all]
            [raiseup.ontime.command-handler :refer :all]
            [raiseup.ontime.readmodel :refer :all]
            [bouncer [core :as b] [validators :as v]]
            [raiseup.ontime.query :as q]
            [common.convert :as convert]))


(defn index-view
  "fetch data for index view"
  [params]
  (let [slots (sort-by :ar-id >
                       (q/find-slots-for-user 1))
        grouped-slots (group-by #(nil? (:start-time %)) slots)]
    {:unplanned-slots (grouped-slots true)
     :planned-slots (grouped-slots false)
     :date-fmt (fn [text f]
                 (convert/->str
                  (convert/->date
                   (convert/->long (f text)))))}))

(defn create-task-slot-action
  "create an task slot"
  [params]
  (cqrs/send-command
   (->CreateTaskSlot :task-slot nil 1 (:description params) nil 40)))

(defn delete-task-slot-action
  [req]
  (cqrs/send-command
   (->DeleteTaskSlot :task-slot (:ar-id req) 1)))

(defn start-task-slot-action
  "action to start task slot"
  [params]
  (cqrs/send-command
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
