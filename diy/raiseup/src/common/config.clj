(ns ^{:doc "config toolbox"}
  common.config)

(def dev-config-file "config-dev.clj")

(defn env
  "get system env property"
  ([key default]
     (System/getProperty (name key) default))
  ([key]
     (env key "")))

(defn dev-mode?
  ^{:doc "is in the dev mode"
    :side-affect "read system property"}
  []
  (empty? (env :production)))

(defn read-edn-file
  [file]
  (read (java.io.PushbackReader.
         (-> file clojure.java.io/resource clojure.java.io/reader))))



(defn read-config
  ^{:doc "read config file, if production mode is activated,
          the specified config will merge the dev configs"
    :side-affect "read config file"}
  []
  (if (dev-mode?)
    (read-edn-file (env :config dev-config-file))
    (if (empty? (env :config))
      (throw
       (java.lang.IllegalArgumentException.
        "config file is not specified for production mode"))
      (merge (read-edn-file dev-config-file)
             (read-edn-file (env :config))))))

(defn ret
  ^{:doc "return a config by key"}
  ([key]
     ((read-config) key))
  ([k1 k2]
     (get-in (read-config) [k1 k2]))
  ([k1 k2 k3]
     (get-in (read-config) [k1 k2 k3])))
