(ns ^{:doc "the read model implemented by elastic search"}
  cqrs.elastic-rm
  (:require [cqrs.protocol :as cqrs]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.rest :as es]
            [common.logging :as log]
            [clojurewerkz.elastisch.rest.document :as esd])
  (:import (common.component Lifecycle)))


(defrecord ElasticReadModel [app]
  cqrs/ReadModel
  (load-entry [this entry-type entry-id]
    (:_source (esd/get app (name entry-type) (str entry-id))))
  (update-entry [this entry-type entry-id f]
    (let [old-entry (.load-entry this entry-type (str entry-id))]
      (.put-entry this (f old-entry))))
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
      (if (empty? query-result) []
          (map #(get % :_source) (-> query-result :hits :hits)))))
  Lifecycle
  (init [this options]
    (try
      (do
        (es/connect! (:url options))
        (if-not (idx/exists? app)
          (do
            (idx/create (:app options)
                        :settings (:settings options)
                        :mappings (:mappings options))
            (log/debug "creating elastic search index " app))
          (log/debug "starting elastic search index " app)))
      (catch Exception e
        (log/error e)))
    this)
  (start [this options]
    (log/debug "starting elastic search"))
  (stop [this options]
    ()))
