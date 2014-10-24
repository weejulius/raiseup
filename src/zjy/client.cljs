(ns zjy.client
  (:require [figwheel.client :as fw :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as d :include-macros true]
            [dommy.core :as dom]
            [ajax.core :refer [POST GET easy-ajax-request raw-response-format
                               detect-response-format]])
  (:use-macros
   [cljs.core.async.macros :only [go]]
   [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

#_(fw/watch-and-reload
 :websocket-url   "ws://localhost:3449/figwheel-ws"  ;; :websocket-url "ws://localhost:3449/figwheel-ws" default
 :jsload-callback (fn [] (print "reloaded")))


(defn on-init-response
  [state response]
  (om/transact! state (fn [state]
                              (assoc state :game-data response))))


(defn init
  [state]
  (dom/listen!
   (sel1 :#read-role) :click
   (fn [event]
     (easy-ajax-request
      "/zjy/q" :get
      {:params {:game_server (.-value (sel1 :#game-server))
                :game_name (.-value (sel1 :#game-name))}
       :handler (partial on-init-response state)
       :response-format (raw-response-format)}))))

(defn app-component
  [state owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (init state))
    om/IRenderState
    (render-state [_ _]
      (d/span #js {:id "game-data"}
              (d/div #js {} (:game-data state))))))

(def state (atom {:game-name "what are you!----++ss-..."
                        :game-data nil}))

(defn index []
  (om/root app-component state {:target (sel1 :#role-info)}))
