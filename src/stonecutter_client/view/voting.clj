(ns stonecutter-client.view.voting
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))


(defn set-user-name [request enlive-m]
  (let [user-email (get-in request [:session :user])]
  (html/at enlive-m 
           [:.clj-user] (html/content user-email))))

(defn voting-page [request]
  (->> (vh/load-template "public/poll.html")
       #_(set-user-name request)
        html/emit*
       (apply str)))

