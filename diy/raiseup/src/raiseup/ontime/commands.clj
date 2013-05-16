(ns raiseup.ontime.commands)

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
