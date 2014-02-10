(ns cqrs.t-core
  (:use [midje.sweet]
        [cqrs.core])
  (:require [clojure.core.async :as async :refer [<! take! put! <!! >! >!! go chan timeout alts! alts!! alt!]]))

(fact "get ar state by replaying events"
  (get-ar [{:ar :task}
           {:ar :task :name "hello"}
           {:ar :task :name "hello1" :nick "bob"}])
  => {:ar :task :name "hello1" :nick "bob" :vsn 3})

(defn handler [c d]
  (go
   (let []
     (loop []
       (when-let [v (<! c)]
         (println "2" (java.util.Date.))
         (Thread/sleep 2000)
         (println "3" (java.util.Date.) v )
         (>! d (str "!" v))
         (recur))
       (println "exiting...")))))


(fact "test timeout"
  (let [c1 (chan)
        c2 (chan)]
    (handler c1 c2)
    (dotimes [n 3]
      (println "1" (java.util.Date.))
      (go  (>! c1 "hello"))
      (<!! (go
            (alts! [c2 (timeout 1000)])))
      (println "4" (java.util.Date.))
      ""
      => nil?)))


(let [c1 (chan)
      c2 (chan)]
  (go (while true
        (let [[v ch] (alts! [c1 c2])]
          (println "Read" v "from" ch))))
  (go (>! c1 "hi"))
  (go (>! c2 "there")))
