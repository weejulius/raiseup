(ns raiseup.ontime.model)

(defprotocol Task-protocol
  "task is a unit of work which can be done round 30 min,
   and it has clear goal and is easy to determine if it is done, we can try again if
   the task is not achieved within estimation."

  (attempt [this estimation current-time]
    "attempt to complete the task within the estimation")

  (stop-attempt [this current-time]
    "when the estimation is over,the attempt is supposed to be stopped
     and the task should be completed as well")

  (interrupt-attempt [this current-time interruptted-reason]
    "when there are urgent things to be handled instantly, the attempt is
     interrupted with reason"))

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
           ;the lastest attempt is on the top among the attempts
           (#(update-in % [:attempts] conj an-attempt)))))

  (stop-attempt [this current-time]
    (let [the-start-attempt (first (:attempts this))
          the-stop-attempt (->> the-start-attempt
                        (#(assoc % :status :attempt-done))
                        (#(assoc % :stopped-time current-time)))]
      (->> this
           (#(assoc % :task-status :task-done))
           (#(assoc % :task-done-time current-time))
           (#(assoc-in % [:attempts 0] the-stop-attempt)))))

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
