(ns notes.web.client
  (:require
    [ajax.core :refer [POST]]
    [dommy.core :as dom]
    [reagent.core :as reagent :refer [atom]])
  (:use-macros
    [dommy.macros :only [node sel sel1]]))

(defn- now-as-mills
  []
  (.getTime (js/Date.)))

(defn- val
  [el]
  (.-value (sel1 el)))

(defn- log
  [msg]
  (.log js/console msg))


(def auto-save-time-state (atom (js/Date.)))

(def check-auto-save-interval 2000)


(defn- resp-save-note-command
  [response]
  (reset! auto-save-time-state (js/Date.)))

(defn- send-update-note-command
  []
  (POST "/notes/commands"
        {:params   {"command" (str {:ar      :note
                                    :command :update-note
                                    :utime   (.getTime (js/Date.))
                                    :title   nil
                                    :content (val :#content)
                                    :ar-id   (int (val :#note-id))})}
         :handler  resp-save-note-command
         :format   :raw
         :keyword? true}))



(defn- auto-save-note
  []
  (js/setTimeout
    #(send-update-note-command) 60000)
  )

(defn update-auto-save-note-tip
  []
  (auto-save-note)
  [:span (str "Tip: note is auto saved at " @auto-save-time-state)])

(defn ^:export run []
  (reagent/render-component [update-auto-save-note-tip]
                            (sel1 :#auto-save-tip)))