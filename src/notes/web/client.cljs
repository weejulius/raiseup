(ns notes.web.client
  (:require
    [ajax.core :refer [POST]]
    [reagent.core :as reagent :refer [atom]]))

(def save-time-state (atom (js/Date.)))

(defn- resp-save-note-command
  [response]
  (.log js/console (str response))
  (reset! save-time-state (js/Date.)))

(defn- send-update-note-command
  []
  (POST "/notes/commands"
        {:params   {"command" (str {:ar      :note
                                    :command :update-note
                                    :utime   (.getTime (js/Date.))
                                    :title   nil
                                    :content (.-value (.getElementById js/document "content"))
                                    :ar-id   (int (.-value (.getElementById js/document "note-id")))})}
         :handler  resp-save-note-command
         :format   :raw
         :keyword? true}))

(defn- auto-save-note
  []
  (js/setTimeout #(send-update-note-command) 30000))

(defn update-auto-save-note-tip
  []
  (auto-save-note)
  [:span (str "Tip: note is auto saved at " @save-time-state)])

(defn ^:export run []
  (reagent/render-component [update-auto-save-note-tip]
                            (.getElementById js/document "auto-save-msg")))