(ns common.t-logging
  (:require [midje.sweet :refer :all]
            [common.logging :as log]))

(fact "info logging"
  (log/info "hello word"))
