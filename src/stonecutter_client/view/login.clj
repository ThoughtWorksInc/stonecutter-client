(ns stonecutter-client.view.login
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))

(defn login-page [request]
  (->> (vh/load-template "public/index.html")
        vh/add-anti-forgery
        html/emit*
       (apply str)))


