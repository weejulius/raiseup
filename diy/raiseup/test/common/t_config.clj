(ns t-config
  (:require [common.config :as cfg]
            [midje.sweet :refer :all]))

(fact "read config file"
  (cfg/read-edn-file "config-dev.clj") => (complement nil?))


(fact "read key"
  (cfg/ret :charset) => "UTF-8")

(fact "read nested key"
  (cfg/ret :es :recoverable-ar-id-key) => "rvb-ar-id-key")

(fact "read unknown key throw exception"
  (cfg/ret :un)=> (throws java.lang.IllegalArgumentException ":un not found"))
