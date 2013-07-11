(ns raiseup.ontime.domain)

(defn create-event
 [event]
 (assoc event :event-dt (java.util.Date.)))

(defn create-task
  "create a task"
  [id description owner estimation]
  (create-event {:id id
                 :description description
                 :owner owner
                 :estimation estimation}))


(defn on-task-created
  [ar event]
  (merge ar event))

(defn on-task-deleted
  [ar event]
  (merge ar event))
