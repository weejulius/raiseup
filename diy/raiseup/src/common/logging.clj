(ns ^{:doc "a logging tool impl or wrapper"}
  common.logging
  (:require [taoensso.timbre :as timbre]))


(def ^:private ordered-levels1 [:trace :debug :info :warn :error :fatal :report])

(defmacro def-logger1 [level]
  (let [level-name (name level)]
    `(do
       (defmacro ~(symbol level-name)
         ~(str "Log given arguments at " level " level using print-style args.")
         ~'{:arglists '([& message] [throwable & message])}
         [& sigs#] `(timbre/logp ~~level ~@sigs#))

       (defmacro ~(symbol (str level-name "f"))
         ~(str "Log given arguments at " level " level using format-style args.")
         ~'{:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
         [& sigs#] `(timbre/logf ~~level ~@sigs#)))))

(defmacro def-loggers1 []
  `(do ~@(map (fn [level] `(def-logger1 ~level)) ordered-levels1)))



(def-loggers1) ; Actually define a logger for each logging level
