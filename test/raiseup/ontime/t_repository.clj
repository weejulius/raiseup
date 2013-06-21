(ns raiseup.ontime.t-repository
  (:use midje.sweet
        raiseup.ontime.model
        raiseup.ontime.repository))

(def new-task (create-task "new task" "jyu" 10 nil))

(fact "transfer domain model to datomic data structure"
      (record-to-datomic new-task :task) => {:task/description "new task"
                                             :task/estimation 10
                                             :task/owner "jyu"
                                             :task/created-time nil
                                             :task/status :created
                                             :task/attempts []})
