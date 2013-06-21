(ns raiseup.ontime.t-infrastructure
  (:use [datomic.api :only (q db entity resolve-tempid) :as d]
        midje.sweet
        raiseup.ontime.infrastructure
        raiseup.ontime.model))

(def uri "datomic:free://localhost:4334//raiseup")

(d/create-database uri)
(def conn (d/connect uri))

(def schema-tx (read-string (slurp "resources/schema.edn")))

(d/transact conn schema-tx)

(def data-tx [{:attempt/estimation 10
               :attempt/started-time (java.util.Date.)
               :attempt/stopped-time (java.util.Date.)
               :attempt/status "STARTED"
               :db/id #db/id[:db.part/user -10001]}
              {:task/description "task one"
               :task/estimation 30
               :task/owner "jyu"
               :task/created-time (java.util.Date.)
               :task/status "STARTED"
               :task/attempts [#db/id[:db.part/user -10001]]
               :db/id #db/id[:db.part/db]}])

(d/transact conn data-tx)

(defn make-transaction
  [data-tx]
  (let [tempid (get-in data-tx [0 :db/id])
        {:keys [tempids db-after]} @(d/transact conn data-tx)
        real-id  (d/resolve-tempid db-after tempids tempid)]
    (fn [fun] (fun db-after real-id))))

(fact
 (:task/estimation
  (((store-new-task
     (create-task "new task" "jyu" 10 (java.util.Date.))) make-transaction)
   d/entity))
 => 10)

(fact (:attempt/status
       (d/entity (d/db conn) (ffirst
                              (q '[:find ?a
                                   :where
                                   [?t :task/owner "jyu"]
                                   [?t :task/attempts ?a]
                                   [?a :attempt/status "STARTED"]]
                                 (d/db conn)))))=> :STARTED)
