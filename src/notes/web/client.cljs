(ns notes.web.client
  (:require
    [ajax.core :refer [POST]]
    [dommy.core :as dom]
    [reagent.core :as reagent :refer [atom]]
    [goog.storage.mechanism.HTML5SessionStorage :as html5ss])
  (:use-macros
    [dommy.macros :only [node sel sel1]]))

(defn- now-as-mills
  []
  (.getTime (js/Date.)))

(defn- val-for-el
  [el]
  (.-value (sel1 el)))

(defn- log
  [msg]
  (.log js/console msg))



(def storage
  (goog.storage.mechanism.HTML5SessionStorage.))

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
                                    :utime   (now-as-mills)
                                    :title   (val-for-el :.title)
                                    :content (val-for-el :#content)
                                    :ar-id   (int (val-for-el :#note-id))})}
         :handler  resp-save-note-command
         :format   :raw
         :keyword? true}))


(defn- save-note-to-session-storage
  [session-storage key note]
  (.set session-storage key note)
  ;(reset! auto-save-time-state (js/Date.))
  )

(defn- storage-key
  []
  (str "note" (val-for-el :#note-id)))
(defn- save-note-local
  []
  (save-note-to-session-storage
    storage
    (storage-key)
    (val-for-el :#content)))

(defn- auto-save-note
  []
  (js/setTimeout
    #(save-note-local) 4000))

(defn- clear-note-local-storage
  []
  (doall
    (map (fn [el]
           (dom/listen! el :click
                        (fn [evt]
                          (.remove storage (storage-key)))))
         (sel :.act))))


(defn- pull-content-from-local
  []
  (let [content (.get storage (storage-key))]
    (if-not (empty? content)
      (dom/set-text! (sel1 :#content) content))))

(defn- listen-save-note-key-press
  []
  (dom/listen! (sel1 :body)
               :keyup
               (fn [event]
                 (let [keycode (.-keyCode event)
                       shift-key (.-shiftKey event)
                       ctrl-key (.-ctrlKey event)
                       save-note? (and shift-key ctrl-key (= keycode 83))]
                   (log (str keycode shift-key ctrl-key))
                   (if save-note?
                     (send-update-note-command))))))

(defn- update-auto-save-note-tip
  []
  (auto-save-note)
  [:span (str "Tip: note is auto saved at " @auto-save-time-state)])

(defn ^:export run []
  (reagent/render-component [update-auto-save-note-tip]
                            (sel1 :#auto-save-tip)))