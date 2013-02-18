(ns raiseup.ontime.model)

(defprotocol Task-protocol
  "task is a unit of work which can be done round 30 min,
  and it has clear goal and is easy to determine if it is done, we can try again if
  the task is not achieved within estimation."

  (attempt [this estimation current-time]
    "attempt to complete the task within the estimation")

  (stop-attempt [this current-time stop-reason]
    "when the estimation is over or unexpected urgent things to
    handle, we stop the attempt"))

(defrecord Task
  [task-id description task-owner task-estimation created-time task-status attempts]
  Task-protocol
  (attempt [this attempt-estimation current-time]
    (let [an-attempt
    {:status :attempt-started
     :started-time current-time
     :attempt-estimation attempt-estimation}]
      (->> this
           (#(assoc % :task-status :in-process))
           (#(update-in % [:attempts] conj an-attempt)))))

  (stop-attempt [this current-time stop-reason]
    (let [the-start-attempt (last (:attempts this))
          the-stop-attempt (->> the-start-attempt
                        (#(assoc % :status :attempt-done))
                        (#(assoc % :stopped-time current-time)))]
      (->> this
           (#(assoc % :task-status :task-done))
           (#(assoc % :task-done-time current-time))
           (#(assoc-in % [:attempts 0] the-stop-attempt))))))
          ; (#(update-in % [:attempts] conj the-stop-attempt))))))


(defn create-task "create one task"
  ([task-id description task-owner estimation created-time]
     {:pre [(number? task-id)
            (string? description)
            (number? estimation) ]}
     (->Task task-id description task-owner estimation created-time :created [])))
