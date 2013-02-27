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

  (attempt
    ^{:added "1.0"}
    [this attempt-estimation current-time]
    (let [an-attempt {:status :started
                      :started-time current-time
                      :estimation attempt-estimation}
          updated-attempts (cons an-attempt (:attempts this))]
      (assoc (assoc this :status :started)
        :attempts (vec updated-attempts))))

  (stop-attempt
    ^{:added "1.0"}
    [this current-time]
    (let [the-start-attempt (get-in this [:attempts 0])]
      (when-not(= :started (:status the-start-attempt))
        (throw (java.lang.IllegalArgumentException.
                "the task is not able to be stopped before it is started")))
      (assoc-in this
                [:attempts 0]
                (->> the-start-attempt
                     (#(assoc % :status :stopped))
                     (#(assoc % :stopped-time current-time)))))))

(defn create-task "create one task"
  ([task-id description task-owner estimation created-time]
     {:pre [(number? task-id)
            (string? description)
            (number? estimation) ]}
     (->DefaultTask
      task-id description task-owner estimation created-time :created [])))
