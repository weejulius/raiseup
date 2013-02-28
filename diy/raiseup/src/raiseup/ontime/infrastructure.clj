(ns raiseup.ontime.infrastructure
  (:use [datomic.api :only (q db) :as d]))

(defn store-new-task
  "store the new task"
  [task]
  (fn [transact] (transact
                  [{:task/description (:description task)
                    :task/estimation (:estimation task)
                    :task/owner (:owner task)
                    :task/created-time (:created-time task)
                    :task/status (:status task)
                    :db/id #db/id[:db.part/user]}])))
