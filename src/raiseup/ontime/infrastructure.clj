(ns raiseup.ontime.infrastructure
  (:use [datomic.api :only (q tempid db) :as d]
        raiseup.ontime.repository))

(defn store-new-task
  "store the new task"
  [task]
  (fn [transact] (transact
                  [(assoc (record-to-datomic task :task)
                     :db/id (d/tempid :db.part/user))])))
