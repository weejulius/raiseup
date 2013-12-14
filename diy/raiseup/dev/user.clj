(ns user
  (:require [clojure.tools.namespace.repl :refer (refresh refresh-all set-refresh-dirs)]
            [system :as s]
            [common.component :as component])
  (:use  [ontime.handler]))


(defn init []
 (alter-var-root #'s/system component/init {}))

(defn start []
  (alter-var-root #'s/system component/start {:port "8080"
                                        :host "localhost"
                                        :routes  #'app-routes}))

(defn stop []
  (alter-var-root #'s/system component/stop {}))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
