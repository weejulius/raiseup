(ns zjy.client
  (:require [figwheel.client :as fw :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as d :include-macros true]
            [dommy.core :as dom])
  (:use-macros
   [cljs.core.async.macros :only [go]]
   [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

#_(fw/watch-and-reload
 :websocket-url   "ws://localhost:3449/figwheel-ws"  ;; :websocket-url "ws://localhost:3449/figwheel-ws" default
 :jsload-callback (fn [] (print "reloaded")))


(defn app-component
  [input-state owner]
  (reify
    om/IWillMount
    (will-mount [_]
      )
    om/IRenderState
    (render-state [_ _]
      (d/span #js {:id "game-name"}
              (:game-name input-state)))))


(def input-state (atom {:game-name "what are you!----++ss-..."}))

(defn init []
  (dom/listen! (sel1 :#read-role) :click
               (fn [event]
                 )))


(defn index []
  (om/root app-component input-state {:target (sel1 :#game-name)}))
