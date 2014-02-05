(ns ontime.web
  (:require [compojure.core :refer [defroutes GET]]
            [ontime.web.control :as v]
            [ontime.query :as q]
            [common.convert :refer :all]
            [system :as s]))

(defn handle-request
  "handle the http request"
  [request]
  (let [request-type (keyword (:type request))
        request-params (:data request)]
    ((case request-type
       :create-task-slot v/create-task-slot-action
       :delete-task-slot v/delete-task-slot-action
       :start-task-slot v/start-task-slot-action) request-params)))

;;(defroutes ontime-routes
           ;(GET "/ws" []
           ;     #(handle-asyn-request % handle-request))
           ;(GET "/todo/slots/new" []
           ;     (render "index" (v/index-view nil)))
           ;
           ;(GET "/todo/slots/edit/:id" [id]
           ;     (render "index"
           ;             (s/fetch (q/map->QuerySlot {:id (->long id)})))))



