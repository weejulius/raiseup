(ns raiseup.ontime.model)

(defprotocol Task
  "task is a unit of work which can be done round 30 min,and it has clear goal
   and is easy to determine if it is done, we can try again if the task is not
   achieved within estimation."

  (attempt [this estimation current-time]
    "attempt to work on the task with the estimation")

  (stop-attempt [this current-time]
    "stop the attempt")

  (complete [this current-time]
    "the task is completed after attempts")

  (delete [this reason]
    "the task is drop"))

(defrecord DefaultTask
    [task-id description owner estimation created-time status attempts]
  Task
  (attempt [this attempt-estimation current-time]
    (let [an-attempt {:status :attempt-started
                      :started-time current-time
                      :attempt-estimation attempt-estimation}]
      (->> this
           (#(assoc % :task-status :in-process))
           ;;the lastest attempt is on the top among the attempts
           (#(update-in % [:attempts] conj an-attempt)))))

  (stop-attempt [this current-time]
    (let [the-start-attempt (first (:attempts this))]
      (if (the-start-attempt)
        ((assoc-in this [:attempts 0] (->> the-start-attempt
                                           (#(assoc % :status :attempt-done))
                                           (#(assoc % :stopped-time current-time)))))
  (interrupt-attempt [this current-time interruptted-reason]
    (let [the-start-attempt (first (:attempts this))
          the-interrupted-attempt (->> the-start-attempt
                                       (#(assoc % :status :attempt-done))
                                       (#(assoc % :stop-time current-time))
                                       (#(assoc % :interruptted-reason interruptted-reason)))
          (->> this
               (#(assoc-in % [:attempts] th)))]




      )))

(defn create-task "create one task"
  ([task-id description task-owner estimation created-time]
     {:pre [(number? task-id)
            (string? description)
            (number? estimation) ]}
     (->Task task-id description task-owner estimation created-time :created [])))
