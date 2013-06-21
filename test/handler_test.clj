(ns handler-test
  (:use [midje.sweet])
  (:require [clostache.parser :as tpl]))

(fact (tpl/render-resource "templates/index.httl" {}) => (complement nil?))
