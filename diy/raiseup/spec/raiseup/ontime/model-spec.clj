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

(run-specs)
