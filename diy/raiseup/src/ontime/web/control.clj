(ns ontime.web.control
  (:require [cqrs.core :as cqrs]
            [system :as s]
            [ontime.query :as q]
            [ontime.commands :refer :all]
            [ontime.command-handler :refer :all]
            [common.convert :as convert]))


(defn index-view
  "fetch data for index view"
  [params]
  (let [slots (sort-by :ar-id >
                       (s/fetch (q/map->QuerySlot {:user-id 1})))
        grouped-slots (group-by #(nil? (:start-time %)) slots)]
    {:unplanned-slots (grouped-slots true)
     :planned-slots   (grouped-slots false)
     :date-fmt        (fn [text f]
                        (convert/->str
                          (convert/->date
                            (convert/->long (f text)))))}))

(defn create-task-slot-action
  "create an task slot"
  [params]
  (s/send-command :task
    (->CreateTaskSlot :task-slot nil 1 (:description params) nil 40)))

(defn delete-task-slot-action
  [req]
  (s/send-command
    (->DeleteTaskSlot :task-slot (:ar-id req) 1)))

(defn start-task-slot-action
  "action to start task slot"
  [params]
  (s/send-command
    (->StartTaskSlot
      :task-slot
      (convert/->long (:ar-id params))
      (:start-time params))))

