(ns raiseup.ontime.model-spec
  (:use  speclj.core
         raiseup.ontime.model))

(describe "task"
          (it "has identifier"
              (should= 1 (:task-id (->Task 1 "task 1" "ower" nil)))))

(run-specs)
