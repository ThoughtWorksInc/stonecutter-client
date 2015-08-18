(ns stonecutter-client.view.login
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.routes :as routes]
            [stonecutter-client.view.view-helpers :as vh]))

(defn set-form-action [enlive-m protocol]
  (html/at enlive-m [:.clj--login_form]
           (html/set-attr :action (routes/path :login :protocol protocol))))

(defn login-page [request protocol]
  (apply str
         (-> (vh/load-template "public/index.html")
             (set-form-action protocol)
             vh/add-anti-forgery
             vh/set-static-paths
             html/emit*)))


