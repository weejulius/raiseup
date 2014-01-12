(ns ontime.views.index
  (:require [hiccup.form :as form]))

(defn index-view
  []
 [:body
    [:div.time-slot-desc
     [:h1 "Every day is almost the same but different completely"]]
    [:div#module-new-slot
     (form/text-field "description" "")
     (form/submit-button "add task slot")]])
