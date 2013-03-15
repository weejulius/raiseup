(ns raiseup.ontime.t-domain
  (:use  midje.sweet
         raiseup.ontime.domain))


(fact "event has date"
      (:event-dt (create-event {:id "100"})) => (complement nil?))


(fact
 (:owner (on-task-created {} (create-event {:id 100 :owner "jyu"}))) => "jyu")
