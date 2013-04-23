(ns raiseup.ontime.commands)

(defn create-task-slot
  "create a slot for a task which is completed by a bunch of slots"
  [[ar ar-id description start-time estimation]]
  {:ar ar
   :ar-id ar-id
   :description description
   :start-time start-time
   :estimation estimation})
