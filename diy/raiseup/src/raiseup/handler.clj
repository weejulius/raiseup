(ns raiseup.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn app [req]
  {:status  200
     :headers {"Content-Type" "text/html"}
        :body    "hello word"})
