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
    (:_source (esd/get app (name entry-type) (str entry-id))))
  (update-entry [this entry-type entry-id f]
    (let [old-entry (cqrs/load-entry this entry-type (str entry-id))]
      (cqrs/put-entry this (f old-entry))))
  (put-entry [this new-entry]
    (do
      (esd/put app
               (name (:ar new-entry))
               (str (:ar-id new-entry))
               new-entry)
      (idx/refresh app)))
  (remove-entry [this entry-type entry-id]
    (do
      (esd/delete app
                  (name entry-type)
                  (str entry-id))
      (idx/refresh app)))
  (do-query [this entry-type query]
    (let [query-result (apply esd/search app (name entry-type) query)]
      (if (empty? query-result)
        []
        (map #(get % :_source) (-> query-result :hits :hits)))))
  component/Lifecycle
  (init [this options]
    (run-local-command (:start-shell options))
    (let [app (:app options)]
      (es/connect! [[(:host options) (:port options)]]
                   {"cluster.name" (:cluster-name options)})
      (if-not (idx/exists? app)
        (do
          (idx/create (:app options)
                      :settings (:settings options)
                      :mappings (:mappings options))
          (log/debug "==== ==== creating elastic search index " app))
        (do
          (doseq [[type map] (:mappings options)]
            (idx/update-mapping (:app options) type :mapping {type map}))
          (log/debug "==== ==== starting existing elastic search index " app))))
    (assoc this :app (:app options)))
  (start [this options]
    (log/debug "==== ==== starting elastic search")
    this)
  (stop [this options]
    this)
  (halt [this options]
    (run-local-command (:shutdown-shell options))
    this))
