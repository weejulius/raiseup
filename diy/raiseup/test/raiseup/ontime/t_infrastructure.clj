(ns raiseup.ontime.t-infrastructure
  (:use [datomic.api :only (q db) :as d]
        midje.sweet))

(def uri "datomic:free://localhost:4334//raiseup")

(d/create-database uri)
(def conn (d/connect uri))

(def schema-tx (read-string (slurp "resources/schema.edn")))

@(d/transact conn schema-tx)

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
               :db/id #db/id[:db.part/user -1000001]}])

@(d/transact conn data-tx)

;(def results (q '[:find ?n :where [?n :task/owner "jyu"]] (db conn)))

;(def id (ffirst results))

;(def entity (-> conn db (d/entity id)))

;(fact (:task/description entity)=> "task one")

(fact (:task/attempts
       (d/entity (d/db conn) (ffirst
                     (q '[:find ?t ?a
                          :where
                          [?t :task/owner "jyu"]
                          [?t :task/attempts ?a]
                          [?a :attempt/status "STARTED"]]
                        (d/db conn)))))=> "STARTED")

