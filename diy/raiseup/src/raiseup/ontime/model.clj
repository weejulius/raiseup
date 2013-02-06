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
  [task-id description task-owner created-time]
  Task-protocol
  (attempt [this attempt-id estimation current-time]
    {:attempt-id attempt-id :task-id task-id :status :attempte-started
     :start-time current-time :estimation estimation})
  (stop-attempt [this attempt-id current-time stop-reason]
    {:attempt-id attempt-id :task-id task-id :status :attempt-done
     :stopped-time current-time}))

(defn create-task "create one task"
  [task-id description task-owner created-time]
  {:pre [(number? task-id)
         (string? description)]}
  (->Task task-id description task-owner created-time))
