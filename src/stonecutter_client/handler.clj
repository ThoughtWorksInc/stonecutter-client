(ns stonecutter-client.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as r]
            [ring.adapter.jetty :refer [run-jetty]]
            [bidi.bidi :refer [path-for]]
            [environ.core :refer [env]]
            [scenic.routes :refer [scenic-handler]]
            [stonecutter-client.routes :refer [routes path]]
            [stonecutter-client.logging :as log-config]
            [stonecutter-client.view.login :as login]
            [stonecutter-client.config :as config]
            [clojure.tools.logging :as log]))

(defn html-response [s]
  (-> s
      r/response
      (r/content-type "text/html")))

(defn home [request]
  (r/redirect (path :login)))

(defn show-login-form [request]
  (html-response (login/login-page request)))

(def client-id (:client-id config/environment))

(def callback-uri "http://localhost:3001/callback")

(def oauth-path (str (:scauth-path config/environment) "/authorisation"))
(def sauth-path (str (:scauth-path config/environment) "/authorisation?client_id=" client-id "&response_type=code&redirect_uri=" callback-uri ))


(defn login [request]
  ; (prn "IN LOGIN HANDLER" request)
  ; (prn "CLIENT" client-id)
  ; (prn "URI" callback-uri)
  (let [response
        (-> (r/redirect sauth-path)
            (assoc :params {:client_id client-id :response_type "code" :redirect_uri callback-uri})
            (assoc-in [:headers "accept"] "text/html"))]
    ; (prn "RETURNING" response)
    response))

(defn oauth-callback [request]
  (html-response "in call back")
  )

(defn not-found [request]
  (html-response "404 PAGE NOT FOUND"))

(def handlers
  {:home                 home
   :show-login-form      show-login-form
   :login                login
   :oauth-callback       oauth-callback
   })

(def app-handler
  (scenic-handler routes handlers not-found))

(defn wrap-error-handling [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error e)
        (-> (html-response "internal server error") (r/status 500))))))

(def wrap-defaults-config
  (-> site-defaults
      (assoc-in [:session :cookie-attrs :max-age] 3600)))

(def app 
  (-> app-handler 
      (wrap-defaults wrap-defaults-config)))

(def port (Integer. (get env :port "4000")))

(defn -main [& args]
  (log-config/init-logger!)
  (-> app wrap-error-handling (run-jetty {:port port})))

