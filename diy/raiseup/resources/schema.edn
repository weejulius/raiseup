[
 ;; task
 {:db/id #db/id[:db.part/db]
  :db/ident :task/description
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/fulltext true
  :db/doc "a brief description of task"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :task/estimation
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one
  :db/doc "an estimation of the task"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :task/owner
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "the person who creates the task"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :task/created-time
  :db/valueType :db.type/instant
  :db/cardinality :db.cardinality/one
  :db/doc "the time creating the task"
  :db.install/_attribute :db.part/db}


 {:db/id #db/id[:db.part/db]
  :db/ident :task/status
  :db/valueType :db.type/keyword
  :db/cardinality :db.cardinality/one
  :db/doc "the status of the task"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :task/attempts
  :db/valueType :db.type/ref
  :db/cardinality :db.cardinality/many
  :db/doc "every attempt for the task"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :attempts
  :db/valueType :db.type/ref
  :db/cardinality :db.cardinality/many
  :db/isComponent true
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :attempt/status
  :db/valueType :db.type/keyword
  :db/cardinality :db.cardinality/one
  :db/doc "the current status of the attempt, started or stopped"
  :db.install/_attribute :db.part/db}


 {:db/id #db/id[:db.part/db]
  :db/ident :attempt/started-time
  :db/valueType :db.type/instant
  :db/cardinality :db.cardinality/one
  :db/doc "the started time of the attempt"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :attempt/estimation
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one
  :db/doc "the started time of the attempt"
  :db.install/_attribute :db.part/db}

  {:db/id #db/id[:db.part/db]
  :db/ident :attempt/stopped-time
  :db/valueType :db.type/instant
  :db/cardinality :db.cardinality/one
  :db/doc "the stopped time of the attempt"
  :db.install/_attribute :db.part/db}
 ]