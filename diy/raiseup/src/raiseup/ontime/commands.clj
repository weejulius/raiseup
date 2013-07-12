(ns ^{:doc "the command handlers"
      :added "1.0"}
  raiseup.ontime.commands)

(defn create-task-slot
  "create a slot for a task which is completed by a bunch of slots"
  [command]
  (let [{:keys [ar ar-id user-id description start-time estimation]} command]
    {:event :task-slot-created
     :ar ar
     :ar-id ar-id
     :description description
     :start-time start-time
     :estimation estimation
     :user-id user-id
     :ect (java.util.Date.)}))

(defn delete-task-slot
  [command]
   {:event :task-slot-deleted
   :ar (:ar command)
   :ar-id (:ar-id command)
   :user-id (:user-id command)
   :ect (java.util.Date.)})
