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
    (esd/put app (name (:ar new-entry)) (str (:ar-id new-entry)) new-entry))
  (remove-entry [this entry-type entry-id]
    (esd/delete app (name entry-type) (str entry-id)))
  (do-query [this entry-type query]
    (do (apply esd/search app (name entry-type) query)))
  Lifecycle
  (init [this options]
    (do
      (es/connect! (:url options))
      (if-not (idx/exists? app)
        (do
          (idx/create (:app options)
                      :settings (:settings options)
                      :mappings (:mappings options))
          (println "creating elastic search index " app)))
      this))
  (start [this options]
    (println ""))
  (stop [this options]
    ()))
