(ns raiseup.ontime.t-infrastructure
  (:use [datomic.api :only (q db) :as d]
        midje.sweet))

(def uri "datomic:free://localhost:4334//raiseup")

(d/create-database uri)
(def conn (d/connect uri))

(def schema-tx (read-string (slurp "resources/schema.edn")))
(println "schema-tx:")
(print schema-tx)

@(d/transact conn schema-tx)

(def data-tx [{:task/description "task one"
               :task/estimation 30
               :task/owner "jyu"
               :task/created-time (java.util.Date.)
               :task/status "STARTED"
               :db/id #db/id[:db.part/user -1000001]}])

@(d/transact conn data-tx)

(def results (q '[:find ?n :where [?n :task/owner "jyu"]] (db conn)))
(print results)
(def id (ffirst results))
(print id)
(def entity (-> conn db (d/entity id)))
(print (keys entity))
(fact (:task/description entity)=> "task one")
