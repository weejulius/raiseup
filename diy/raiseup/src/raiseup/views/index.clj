(ns raiseup.views.index
  (:require [hiccup.form :refer [text-field  submit-button]]))

(defn index-view
  []
 [:body
    [:div.time-slot-desc
     [:h1 "Every day is almost the same but different completely"]]
    [:div#module-new-slot
     (text-field "description")
     (submit-button "add task slot")]])
