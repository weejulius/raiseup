(ns ^{:doc "the read model implemented by elastic search"}
  cqrs.elastic-rm
  (:require [cqrs.protocol :as cqrs]
            [common.component :as component]
            [clojurewerkz.elastisch.native.index :as idx]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.document :as esd]
            [common.logging :as log]
            [clojure.java.shell :as shell]))


(defn- run-local-command
  [cmd]
  (if-not (empty? cmd)
    (apply shell/sh cmd)))

(defrecord ElasticReadModel [app]
  cqrs/ReadModel
  (load-entry [this entry-type entry-id]
    (:_source (esd/get (:conn this) app (name entry-type) (str entry-id))))
  (update-entry [this entry-type entry-id f]
    (let [old-entry (cqrs/load-entry this entry-type (str entry-id))]
      (cqrs/put-entry this (f old-entry))))
  (put-entry [this new-entry]
    (do
      (esd/put
       (:conn this)
       app
       (name (:ar new-entry))
       (str (:ar-id new-entry))
       new-entry)
      (idx/refresh (:conn this) app)))
  (remove-entry [this entry-type entry-id]
    (do
      (esd/delete (:conn this) app
                  (name entry-type)
                  (str entry-id))
      (idx/refresh (:conn this) app)))
  (do-query [this entry-type query]
    (let [query-result (apply esd/search (:conn this) app (name entry-type) query)]
      (if (empty? query-result)
        []
        (map #(get % :_source) (-> query-result :hits :hits)))))


  component/Lifecycle
  (init [this options]
    (run-local-command (:start-shell options))
    (let [app (:app options)
          conn (es/connect [[(:host options) (:port options)]]
                           {"cluster.name" (:cluster-name options)})]
      (assoc this :conn conn)
      (if-not (idx/exists? conn app)
        (do
          (idx/create conn (:app options)
                      :settings (:settings options)
                      :mappings (:mappings options))
          (log/debug "==== ==== creating elastic search index " app))
        (do
          (doseq [[type map] (:mappings options)]
            (idx/update-mapping conn (:app options) type
                                :mapping {type map}
                                :ignore_conflicts true))
          (log/debug "==== ==== starting existing elastic search index " app)))
      (assoc this :app (:app options) :conn conn)))
  (start [this options]
    (log/debug "==== ==== starting elastic search")
    this)
  (stop [this options]
    this)
  (halt [this options]
    (run-local-command (:shutdown-shell options))
    this))
