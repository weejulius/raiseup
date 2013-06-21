(ns raiseup.ontime.t-model
  (:use midje.sweet
        raiseup.ontime.model))

(def new-task (create-task "task 1" "owner" 30 nil))
(fact "attempt to work on a task"
      (get-in (attempt new-task 30 nil)
              [:attempts 0 :status]) => :started)

(fact "stop working on the task"
      (let [started-task (attempt new-task 30 nil)]
        (get-in
         (stop-attempt started-task nil)
         [:attempts 0 :status]) => :stopped))

(fact "cannot stop the task if it is not started"
      (stop-attempt new-task nil) =>
      (throws java.lang.IllegalArgumentException))

(fact (undefined-id) => -1)

(fact "reattempt to work on one task which is not
       completed with the first attempt"
      (let [started-task (attempt new-task 30 nil)
            paused-task (stop-attempt started-task nil)
            reattempted-task (attempt paused-task 10 nil)]
        (count (:attempts reattempted-task)) => 2
        (get-in reattempted-task [:attempts 0 :status]) => :started
        (get-in reattempted-task [:attempts 1 :status]) => :stopped))
