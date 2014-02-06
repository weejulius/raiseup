(ns common.component
  (:require [common.logging :as log]
            [common.config :as config]))

(defprotocol Lifecycle
  "manage the lifecycle of component"
  (init [this options])
  (start [this options])
  (stop [this options]))

(defonce state {})

(defn- process-component
  [name component config f log-prefix]
  (try
    (do
      (log/info "==== " log-prefix "component" name config)
      (let [processed (f component config)]
        (log/info "==== done,component" name)
        (if-not (= (type component) (type processed))
          (throw (ex-info "invalid return for lifecycle fn of component"
                          {:component name
                           :f         f})))
        processed))
    (catch Exception e
      (log/error e))))

(defn- resolve-dependencies
  [components config]
  (let [dependency-syms (filter #(keyword? (val %)) config)]
    (println "resolve dependency" components config dependency-syms)
    (if-not (empty? dependency-syms)
      (reduce
        (fn [m [k v]]
          (if (nil? (v components))
            (do
              (log/error "depened component is not defined"
                         {:component  (:component config)
                          :dependency v})
              config)
            (assoc m k (v components))))
        config dependency-syms)
      config)))

(defn- process-components
  [f get-component cfg]
  (loop [config cfg
         components {}]
    (if (empty? config)
      components
      (let [[name component-config] (first config)
            component (get-component cfg name)]
        (if (nil? component)
          (do
            (log/error "the component is not defined"
                      {:name   name
                       :config component-config})
            components)
          (recur (next config)
                 (assoc components
                   name
                   (f
                     name
                     component
                     (resolve-dependencies components component-config)))))))))

;;TODO prevent from initing once again without closing
(defn init-components
  []
  (alter-var-root #'state
                  (fn [x]
                    (process-components
                      #(process-component %1 %2 %3 init "init")
                      (fn [config name]
                        (let [sym (-> config name :component)
                              _ (require (symbol (namespace sym)))
                              instance ((resolve sym) nil)]
                          instance))
                      (config/read-edn-file "config.clj")))))


(defn start-components
  []
  (alter-var-root #'state
                  (fn [x]
                    (process-components
                      #(process-component %1 %2 %3 start "start")
                      (fn [config name]
                        (println name #'state)
                        (let [component (get state name)]
                          component))
                      (config/read-edn-file "config.clj")))))


(defn stop-components
  []
  (alter-var-root #'state
                  (fn [x]
                    (process-components
                      #(process-component %1 %2 %3 stop "stop")
                      (fn [config name]
                        (let [component (get state name)]
                          (println name component state)
                          component)

                        )
                      (config/read-edn-file "config.clj")))))