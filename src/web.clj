(ns web
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [compojure.route :as route]
            [notes.query :refer :all]
            [notes.web.view.demo :as demo]
            [notes.web :as notes-web :refer :all]
            [common.reqres :as reqres]
            [common.convert :refer [->long ->map ->str]]
            [common.config :as cfg]
            [common.logging :as log]
            ))

(def template-extension-with-dot (str "." (cfg/ret :template-extension)))

#_(defn- render
  "render the template with the params"
  [file-path params]
  (tpl/render-resource
    (str "templates/" file-path template-extension-with-dot)
    params))

#_(defn handle-asyn-request
  [ring-request f]
  (with-channel ring-request channel
                (if (websocket? channel)
                  (on-receive
                    channel
                    (fn [data]
                      (let [params (->map data)
                            result (f params)]
                        (send! channel
                               (->str {:type
                                        ;;TODO refactor
                                             (clojure.string/replace (name (:type params)) #"-" "_")
                                       :data result}))))))))


(defroutes app-routes
           (context "/notes" [] notes-routes)
           (GET "/403" []
                "Forbidden")
           (GET "/401" []
                "Unauthorized")
           (GET "/demo" [:as req]
                (demo/demo-view))
           (GET "/demo/*" [:as req]
                (demo/demo-view))
           (route/resources "/")
           (route/not-found "PAGE NOT FOUND"))




