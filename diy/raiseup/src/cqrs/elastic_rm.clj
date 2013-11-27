(ns ^{:doc "the read model implemented by elastic search"}
  cqrs.elastic-rm
  (:require [cqrs.protocol :as cqrs]
            [common.component :as component]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.rest :as es]
            [clojurewerkz.elastisch.rest.document :as esd]))


(defrecord ElasticReadModel [app]
  cqrs/ReadModel
  (load-entry [this entry-type entry-id]
    (esd/get app (name entry-type) entry-id))
  (update-entry [this entry-type entry-id f]
    (let [old-entry (.load-entry this entry-type entry-id)]
      (.put-entry this (f old-entry))))
  (put-entry [this new-entry]
    (esd/put app (name (:ar new-entry)) (:ar-id new-entry) new-entry))
  (remove-entry [this entry-type entry-id]
    (esd/delete app (name entry-type) entry-id))
  (do-query [this entry-type query]
    (esd/search app (name entry-type) query)))

(defn init [this options]
  (es/connect! options))

(defn start [this options]
  (do
    (idx/create (:app options)
;                :settings (:settings options)
                :mappings (:mappings options))
    (println "starting elastic search readmodel")))
