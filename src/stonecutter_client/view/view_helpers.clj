(ns stonecutter-client.view.view-helpers
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.tools.logging :as log]))

(defn load-template [path]
  (log/debug (format "Loading template %s from file" path))
  (html/html-resource path))

(defn anti-forgery-snippet []
  (html/html-snippet (anti-forgery-field)))

(defn add-anti-forgery [enlive-m]
  (html/at enlive-m
           [:form] (html/prepend (anti-forgery-snippet))))
