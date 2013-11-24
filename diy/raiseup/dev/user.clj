(ns user
  (:require [clojure.tools.namespace.repl :refer (refresh)]
            [system :as s]
            [env :as env])
  (:use  [ontime.handler]))


(defn init []
 (alter-var-root #'s/system env/init {}))

(defn start []
  (alter-var-root #'s/system env/start {:port "8080"
                                        :host "localhost"
                                        :routes  #'app-routes}))

(defn stop []
  (alter-var-root #'s/system env/stop {}))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
