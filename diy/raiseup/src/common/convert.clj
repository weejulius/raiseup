(ns common.convert
  (:use [clojure.string :only (join)])
  (:require [taoensso.nippy :refer [freeze thaw]]
            [taoensso.nippy.compression :as compression]
            [cheshire.core :as json]
            [clj-time.format :as t-format]
            [clj-time.coerce :as t-convert]
            [common.config :as cfg])
  (:import (java.nio ByteBuffer)))


(defprotocol Cast
  (->bytes [this] "convert to bytes")
  (->long [this] "convert to long")
  (->map [this] "convert to map")
  (->str [this] "convert to string")
  (->date [this] "convert to date")
  (->data [this] "convert to clojure data structure"))


(extend-protocol Cast
  String
  (->bytes [this]
    (.getBytes ^String this ^String (cfg/ret :charset)))
  (->long [this]
    (Long/parseLong this))
  (->map [this]
    (json/parse-string this true))
  (->str [this] this)
  (->data [this] this)
  (->date [this]
    (t-convert/to-date
     (t-format/parse
      this)))

  nil
  (->bytes [this]
    nil)
  (->long [this]
    nil)
  (->map [this]
    nil)
  (->str [this]
    nil)
  (->data [this]
    nil)
  (->date [this]
    nil)

  Long
  (->bytes [this]
    (-> (ByteBuffer/allocate 8)
        (.putLong this)
        (.array)))
  (->str [this] (str this))
  (->long [this] this)
  (->data [this] this)
  (->date [this] (t-convert/to-date (t-convert/from-long this)))

  clojure.lang.IPersistentVector
  (->bytes [this]
    (freeze this))

  clojure.lang.LazySeq
  (->bytes [this]
    (freeze this))

  clojure.lang.IPersistentMap
  (->bytes [this]
    (freeze this))
  (->str [this]
    (json/generate-string this)))

(extend-protocol Cast
  (Class/forName "[B")
  (->data [this]
    (thaw this))
  (->long [this]
    (.getLong (ByteBuffer/wrap this))) )

(extend-protocol Cast
  (class (java.util.Date.))
  (->str [this]
    (t-format/unparse
     (t-format/formatter (cfg/ret :short-date-format))
     (t-convert/from-date this))))

(defn byte-to-int-array
  "convert bytes to int collection"
  [int-bytes]
  (when-not (nil? int-bytes)
    (let [buffer (ByteBuffer/wrap int-bytes)
          size (/ (count int-bytes) 4)]
      (repeatedly size #(.getInt buffer)))))

