(ns ^{:doc "config toolbox"}
  common.config
  (:require [common.seq :as seq]
            [clojure.edn :as edn])
  (:import (java.io PushbackReader)))

(def dev-config-file "dev.edn")

(def ^{:doc "placehold when the config is not found"} not-found-placehold "_-config-_placehold")

(defn env
  "get system env property"
  ([key default]
   (System/getProperty (name key) default))
  ([key]
   (env key "")))

(defn dev-mode?
  ^{:doc         "is in the dev mode"
    :side-affect "read system property"}
  []
  (empty? (env :production)))

(defn read-edn-file
  [file]
  (with-open [r (PushbackReader.
                  (-> file clojure.java.io/resource clojure.java.io/reader))]
    (edn/read r)))



(defn read-config
  ^{:doc         "read config file, if production mode is activated,
  the specified config will merge the dev configs"
    :side-affect "read config file"}
  []
  (if (dev-mode?)
    (read-edn-file (env :config dev-config-file))
    (if (empty? (env :config))
      (throw
        (IllegalArgumentException.
          ^String (str "config file is not specified for production mode" (env :config))))
      (seq/deep-merge (read-edn-file dev-config-file)
             (read-edn-file (env :config))))))

(defn- exception-when-config-not-found
  "hanle when config not found"
  [f configs keys]
  (let [config (f configs keys not-found-placehold)]
    (if (= config not-found-placehold)
      (throw
        (IllegalArgumentException. ^String (apply str keys " not found"))))
    config))


(defn ret
  ^{:doc "return a config by key"}
  ([key]
   (exception-when-config-not-found
     get (read-config) key))
  ([k1 k2]
   (exception-when-config-not-found
     get-in (read-config) [k1 k2]))
  ([k1 k2 k3]
   (exception-when-config-not-found
     get-in (read-config) [k1 k2 k3])))
