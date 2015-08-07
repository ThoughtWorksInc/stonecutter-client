(ns stonecutter-client.view.voting
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))


(defn set-user-name [request enlive-m]
  (let [user-email (get-in request [:session :user])
        confirmed (get-in request [:session :user-email-confirmed])
        admin? (= "admin" (get-in request [:session :role]))]
    (prn "ADMIN?" admin?)
    (prn "role" (get-in request [:session :role]))
    (html/at enlive-m
             [:.clj-user] (html/content (str user-email " " (if confirmed ":)" ":(") " " (when admin? "wow! Such Admin! Very Power!")))
             [:.clj-logout] (html/html-content "<a href=/logout class=func--logout__link>logout</a>"))))

(defn voting-page [request]
  (->> (vh/load-template "public/poll.html")
       (set-user-name request)
       html/emit*
       (apply str)))
