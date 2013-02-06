(ns raiseup.ontime.model-spec
  (:use  speclj.core
         raiseup.ontime.model))

(describe "An task"
  (it "has identifier"
    (should= 1 (:task-id (create-task 1 "task 1" "ower" nil))))
  (it "has description"
    (should= "task 2" (:description (create-task 2 "task 2" "owner" nil))))
  (it "has owner(creator)"
    (should= "me" (:task-owner (create-task 3 "task 3" "me" nil))))
  (it "has created time"
    (let [now (java.util.Date.)]
      (should= now (:created-time (create-task 4 "task 4" "me" now)))))
  (it "has default estimation (min)"
    (should= 3 (:estimation (create-task 4 "task 4" 3 "me" nil)))))

(describe "An task"
  (it "must has an number identifier"
    (should-throw (create-task nil "task 5" "owner1" nil))
    (should-throw (create-task "a" "task 6" "owner2" nil))
    (should-throw (create-task (Object.) "task 7" "owner3" nil))))

(describe "An task"
  (it "must have description to state the goal"
    (should-throw (create-task 1 nil "owner" nil))
    (should-throw (create-task 1 1 "owner" nil))))


(run-specs)
