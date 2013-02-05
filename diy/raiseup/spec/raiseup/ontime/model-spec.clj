(ns raiseup.ontime.model-spec
  (:use  speclj.core
         raiseup.ontime.model))

(describe "An task"
  (it "has identifier"
    (should= 1 (:task-id (->Task 1 "task 1" "ower" nil))))
  (it "has description"
    (should= "task 2" (:description (->Task 2 "task 2" "owner" nil))))
  (it "has owner(creator)"
    (should= "me" (:task-owner (->Task 3 "task 3" "me" nil))))
  (it "has created time"
    (let [now (java.util.Date.)]
      (should= now (:created-time (->Task 4 "task 4" "me" now))))))

(describe "An task"
  (it "must has an number identifier"
    (should-throw (create-task nil "task 5" "owner1" nil))
    (should-throw (create-task "a" "task 6" "owner2" nil))
    (should-throw (create-task (Object.) "task 7" "owner3" nil)
                  )))

(describe "An task"
  (it "must have description to state the goal"
    (should-throw (create-task 1 nil "owner" nil))
    (should-throw (create-task 1 1 "owner" nil))))
                            

(run-specs)








