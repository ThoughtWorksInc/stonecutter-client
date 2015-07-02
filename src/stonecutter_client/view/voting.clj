(ns stonecutter-client.view.voting
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))

(defn voting-page [request]
  (->> (vh/load-template "public/poll.html")
        html/emit*
       (apply str)))


