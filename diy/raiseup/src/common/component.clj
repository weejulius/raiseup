(ns common.component
  (:require [common.logging :as log]
            [common.config :as config]))

(defprotocol Lifecycle
  "manage the lifecycle of component"
  (init [this options] "init the components")
  (start [this options] "start the components or restart the stopped components")
  (stop [this options] "stop the started components")
  (halt [this options] "shutdown the components when the system is to be down"))

;* Component lifecycle management v1 is done …
;  - TODO
;    - merge configs
;    - auto resolve symbol
;    - component can have its default config
;    - improve component dependency handle
;    - each triggered component is visible, current is visible when the whole is done
;    - solve problem that the defrecord with component must have a field
(defonce state {})

(defn- trigger-component-lifecycle
  [lifecycle-fn lifecycle-name name component config]
  (try
    (do
      (log/info "====" lifecycle-name "component" name config)
      (let [triggered (lifecycle-fn component config)]
        (log/info "==== done,component" name)
        (if-not (= (type component) (type triggered))
          (log/error "invalid return for lifecycle fn of component"
                     {:component name
                      :f         lifecycle-name
                      :before    component
                      :after     triggered}))
        triggered))
    (catch Exception e
      (log/error e (str "fail to trigger lifecycle " lifecycle-name " for  component " name))
      component)))

(defn- resolve-component-dependencies
  [components config]
  (let [dependency-syms (filter #(keyword? (val %)) config)]
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

(defn- trigger-all-component-lifecycle
  [trigger-component-lifecycle get-component cfg]
  (loop [config cfg
         state {}]
    (if (empty? config)
      state
      (let [[name component-config] (first config)
            component (get-component cfg name)]
        (if (nil? component)
          (do
            (log/error "the component is not defined"
                       {:name   name
                        :config component-config})
            state)
          (recur (next config)
                 (assoc state
                   name
                   (trigger-component-lifecycle
                     name
                     component
                     (resolve-component-dependencies state component-config)))))))))

(defn- swap-state
  [state lifecycle-fn lifecycle-name component-get-fn configs]
  (let [next-status-map {:init  [:start :halt]
                         :start [:stop :halt]
                         :stop  [:start :halt]
                         :halt  [:init]}
        current-status (:status state)
        lifecycle-name-as-keyword (keyword lifecycle-name)
        _ (if-not (or (nil? current-status) (some #{lifecycle-name-as-keyword} (current-status next-status-map)))
            (throw (ex-info "cannot trigger component to target status"
                            {:from current-status
                             :to   lifecycle-name-as-keyword})))
        new-state (trigger-all-component-lifecycle
                    (partial trigger-component-lifecycle lifecycle-fn lifecycle-name)
                    component-get-fn
                    configs)]
    (assoc new-state :status lifecycle-name-as-keyword)))

(defn- alter-state-root
  [lifecycle-fn lifecycle-name component-get-fn more-config]
  (alter-var-root
    #'state
    swap-state
    lifecycle-fn
    lifecycle-name
    component-get-fn
    (merge-with merge (config/ret :components) (or more-config {})))
  state)

(defn init-components
  [configs]
  (alter-state-root init "init"
                    (fn [config name]
                      (let [sym (-> config name :component)
                            _ (require (symbol (namespace sym)))
                            instance ((resolve sym) nil)]
                        instance))
                    configs))


(defn start-components
  [configs]
  (alter-state-root start "start"
                    (fn [config name]
                      (let [component (get state name)]
                        component))
                    configs))


(defn stop-components
  [configs]
  (alter-state-root stop "stop"
                    (fn [config name]
                      (let [component (get state name)]
                        component))
                    configs))

(defn halt-components
  [configs]
  (alter-state-root halt "halt"
                    (fn [config name]
                      (let [component (get state name)]
                        component))
                    configs))



(defn go-
  "Initializes and starts the system running."
  [& {:as configs}]
  (try
    (init-components configs)
    (start-components configs)
    state
    (catch Throwable e
      (log/info e "failed to init and start components"))))

(defn stop-
  "stop the components"
  [& {:as configs}]
  (try
    (stop-components configs)
    state
    (catch Throwable e
      (log/info e "failed to stop components"))))


(defn refresh-
  "refresh the components"
  [& {:as configs}]
  (try
    (stop-components configs)
    (start-components configs)
    state
    (catch Throwable e
      (log/info e "failed to stop components"))))

(defn shutdown-
  "shutdown the components"
  [& {:as configs}]
  (try
    (if (= :start (:status state))
      (stop-components configs))
    (halt-components configs)
    state
    (catch Throwable e
      (log/info e "failed to shutdown components"))))