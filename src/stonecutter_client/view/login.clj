(ns stonecutter-client.view.login
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.config :as config]
            [stonecutter-client.view.view-helpers :as vh]))

(def oauth-path (str (:scauth-path config/environment) "/authorisation"))

(def client-id (:client-id config/environment))

(def callback-uri "callback")

(defn set-auth-link [enlive-m]
  (html/at enlive-m 
           [:.clj-home-link-to-auth] (html/set-attr :href (str oauth-path "?client-id=" client-id "&response_type=code&redirect_uri=" callback-uri))))

(defn login-page [request]
  (->> (vh/load-template "public/index.html")
        vh/add-anti-forgery
        html/emit*
       (apply str)))

