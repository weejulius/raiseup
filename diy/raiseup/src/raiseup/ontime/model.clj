(ns raiseup.ontime.model)

(defprotocol Task-protocol "task is a unit of work which can be done round 30 min,
  and it has clear goal and is easy to determine if it is done, we can try again if
  the task is not achieved within estimation."
  (attempt [this attempt-id estimation current-time]
    "attempt to complete the task within the estimation")
  (stop-attempt [this attempt-id current-time stop-reason]
    "when the estimation is over or unexpected urgent things to
    handle, we stop the attempt"))

(defrecord Task
  [task-id description task-owner task-estimation created-time]
  Task-protocol
  (attempt [this attempt-id attempt-estimation current-time]
    {:attempt-id attempt-id :task-id task-id :status :attempte-started
     :start-time current-time :attempt-estimation attempt-estimation})
  (stop-attempt [this attempt-id current-time stop-reason]
    {:attempt-id attempt-id :task-id task-id :status :attempt-done
     :stopped-time current-time}))

(defn create-task "create one task"
  ([task-id description task-owner estimation created-time]
     {:pre [(number? task-id)
            (string? description)
            (number? estimation) ]}
     (->Task task-id description task-owner estimation created-time))
  ([task-id description task-owner created-time]
     (create-task task-id description task-owner (with-default :task-estimation nil)  created-time)))

(defmulti with-default
 ; "if pre is qualified,the default is used"
  (fn[x y] (identity x)))

(defmethod with-default
  ;"populate the empty task estimation with default"
  :task-estimation  [type task-estimation]
  (if(nil? task-estimation) 30 task-estimation))

(with-default :task-estimation nil )

 
