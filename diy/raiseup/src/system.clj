(ns system
  (:require [com.stuartsierra.component :as component]
            [ontime :as ontime]))

(defrecord OnTimeSystem [config-options cache]
  component/Lifecycle
  (start [this]
    (component/start-system this [:cache]))
  (stop [this]
    (component/stop-system this [:cache])))


(defn ontime-system [config-options]
  (let [{:keys [host port]} config-options]
    (map->OnTimeSystem
      {:config-options config-options
       :cache (ontime/new-cache)})))
