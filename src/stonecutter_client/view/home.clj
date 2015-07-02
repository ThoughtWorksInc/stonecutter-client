(ns stonecutter-client.view.home
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))

(defn home-page [request]
  (->> (vh/load-template "public/index.html")
       html/emit*
       (apply str)))
