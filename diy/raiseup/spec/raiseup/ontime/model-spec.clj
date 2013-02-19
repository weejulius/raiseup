(ns raiseup.ontime.model-spec
  (:use  speclj.core
         raiseup.ontime.model))

(describe "An task"
  (it "has identifier"
    (should= 1 (:task-id (create-task 1 "task 1" "ower" 3 nil))))
  (it "has description"
    (should= "task 2" (:description (create-task 2 "task 2" "owner" 3 nil))))
  (it "has owner(creator)"
    (should= "me" (:task-owner (create-task 3 "task 3" "me" 3  nil))))
  (it "has created time"
    (let [now (java.util.Date.)]
      (should= now (:created-time (create-task 4 "task 4" "me" 3 now)))))
  (it "has default estimation (min)"
    (should= 30 (:task-estimation (create-task 4 "task 4" "me" 30 nil)))))

(describe "An task"
  (it "must has an number identifier"
    (should-throw (create-task nil "task 5" "owner1" 3  nil))
    (should-throw (create-task "a" "task 6" "owner2" 3 nil))
    (should-throw (create-task (Object.) "task 7" "owner3" 3 nil))))

(describe "An task"
  (it "must have description to state the goal"
    (should-throw (create-task 1 nil "owner" nil))
    (should-throw (create-task 1 1 "owner" nil))))

(describe "An task"
  (it "must have estimation"
    (should-throw (create-task 1 "task 7" "owner" nil nil))))

(describe "An task"
  (it "is kick off after you attempt to work on it"
    (let [task (create-task 1 "task1" "owner" 30 (java.util.Date.))
          an-attempt (attempt task 30 (java.util.Date.))]
      (should= :in-process (:task-status an-attempt))
      (should= :attempt-started (get-in an-attempt [:attempts 0 :status])))))

(describe "An task"
  (it "is done after you achieve the goal by the end of the attempt"
    (let [task (create-task 1 "task1" "owner" 30 (java.util.Date.))
          an-attempt (attempt task 30 (java.util.Date.))
          the-stop-attempt (stop-attempt an-attempt (java.util.Date.))]
      (should= :attempt-done (get-in the-stop-attempt [:attempts 0 :status])))))

(describe "An task"
  (it "is not done if the attempt is interrupted"
(let [task (create-task 1 "task1" "owner" 30 (java.util.Date.))
an-attempt (attempt task 30 (java.util.Date.))
                            interrupted-attempt (interrupt-attempt an-attempt (java.util.Date.) "I am hungry")])))
(run-specs)
