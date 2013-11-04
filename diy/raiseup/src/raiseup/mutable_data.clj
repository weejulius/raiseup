(ns raiseup.mutable-data
  (:require [common.cache :as c])
  (:import (com.hazelcast.core Hazelcast)
           (com.hazelcast.config Config)))
