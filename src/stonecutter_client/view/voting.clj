(ns stonecutter-client.view.voting
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))


(defn set-user-name [request enlive-m]
  (let [user-email (get-in request [:session :user])
        confirmed (get-in request [:session :user-email-confirmed])]
    (html/at enlive-m
             [:.clj-user] (html/content (str user-email " " (if confirmed ":)" ":(")))
             [:.clj-logout] (html/html-content "<a href=/logout class=func--logout__link>logout</a>"))))

(defn voting-page [request]
  (->> (vh/load-template "public/poll.html")
       (set-user-name request)
       html/emit*
       (apply str)))
