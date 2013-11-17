(ns cqrs.t-core
  (:use [midje.sweet]
        [cqrs.core]))

(fact "get ar state by replaying events"
  (get-ar [{:ar :task}
           {:ar :task :name "hello"}
           {:ar :task :name "hello1" :nick "bob"}])
  => {:ar :task :name "hello1" :nick "bob" :vsn 3})
