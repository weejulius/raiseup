(ns common.convert
  (:use [clojure.string :only (join)])
  (:require [taoensso.nippy :refer [freeze-to-bytes thaw-from-bytes]]
            [cheshire.core :as json]
            [clj-time.format :as t-format]
            [clj-time.coerce :as t-convert]))

(def default-charset "UTF-8")
(def short-date-format "yyyy-MM-dd")
(def long-date-format "yyyy-MM-dd HH:mm:ss")

(defprotocol Cast
  (->bytes [this] "convert to bytes")
  (->long [this] "convert to long")
  (->map [this] "convert to map")
  (->str [this] "convert to string")
  (->data [this] "convert to clojure data structure"))


(extend-protocol Cast

  String
  (->bytes [this]
    (.getBytes this default-charset) )
  (->long [this]
    (Long/parseLong this))
  (->map [this]
    (json/parse-string this true))
  (->str [this] this)
  (->data [this] this)

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

  Long
  (->bytes [this]
    (-> (java.nio.ByteBuffer/allocate 8)
        (.putLong this)
        (.array)))
  (->str [this] (.toString this))
  (->long [this] this)
  (->data [this] this)

  clojure.lang.IPersistentVector
  (->bytes [this]
    (freeze-to-bytes this))

  clojure.lang.LazySeq
  (->bytes [this]
    (freeze-to-bytes this))

  clojure.lang.IPersistentMap
  (->bytes [this]
    (freeze-to-bytes this))
  (->str [this]
    (json/generate-string this)))

(extend-protocol Cast
  (Class/forName "[B")
  (->data [this]
    (thaw-from-bytes this))
  (->long [this]
    (->> (java.nio.ByteBuffer/wrap this)
         (.getLong))) )

(extend-protocol Cast
  (class (java.util.Date.))
  (->str [this]
    (t-format/unparse
     (t-format/formatter short-date-format)
     (t-convert/from-date this))))

(defn byte-to-int-array
  "convert bytes to int collection"
  [int-bytes]
  (if (nil? int-bytes)
    nil
    (let [buffer (java.nio.ByteBuffer/wrap int-bytes)
          size (/ (count int-bytes) 4)]
      (repeatedly size #(.getInt buffer)))))

