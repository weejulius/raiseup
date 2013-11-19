(ns cqrs.t-core
  (:use [midje.sweet]
        [cqrs.core])
  (:require [clojure.core.async :as async :refer [<! take! put! <!! >! >!! go chan timeout alts! alts!! alt!]]))

(fact "get ar state by replaying events"
  (get-ar [{:ar :task}
           {:ar :task :name "hello"}
           {:ar :task :name "hello1" :nick "bob"}])
  => {:ar :task :name "hello1" :nick "bob" :vsn 3})

(defn handler [c]
  (go
   (let []
     (loop []
       (when-let [v (<! c)]
         (println "2" (java.util.Date.))
         (Thread/sleep 4000)
         (println "3" v (java.util.Date.))
         (recur))
       (println "existing...")))))

(fact "test timeout"
  (let [c (chan)]
    (handler c)
    (println "1" (java.util.Date.))
    (go (>! c "hello")
        )
    (println "4" (java.util.Date.))
    ""
    => nil?))
