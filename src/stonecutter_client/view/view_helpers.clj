(ns stonecutter-client.view.view-helpers
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.tools.logging :as log]))

(defn load-template [path]
  (log/debug (format "Loading template %s from file" path))
  (html/html-resource path))

(defn anti-forgery-snippet []
  (html/html-snippet (anti-forgery-field)))

(defn set-static-paths [enlive-m]
  (-> enlive-m 
      (html/at [[:link (html/attr= :rel "icon")]] (html/set-attr :href "/favicon.png"))
      (html/at [:link.clj--stylesheet] (fn [element]
                                         (let [href (get-in element [:attrs :href])]
                                           ((html/set-attr :href (str "/" href)) element))))
      (html/at [:.clj--header__logo] (html/set-attr :href "/"))))

(defn add-anti-forgery [enlive-m]
  (html/at enlive-m
           [:form] (html/prepend (anti-forgery-snippet))))
