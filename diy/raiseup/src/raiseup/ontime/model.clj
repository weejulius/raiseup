(ns raiseup.ontime.model)


(defprotocol Task-protocol "task is a goal which can be done with a short time,
  normally it is from 10 min to 40 min. it is possible that the actual is more
  than the estimation, therefore we might try more than one times to achieve the
  task"
  (attempt [this attempt-id estimation current-time]
    "attempt to complete the task within the estimation,each task can be attempt times")
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