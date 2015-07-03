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
            [stonecutter-client.view.voting :as voting]
            [stonecutter-client.view.view-poll :as view-poll]
            [stonecutter-client.config :as config]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]))

(def client-id (:client-id config/environment))

(def client-secret (:client-secret config/environment))

(def callback-uri "http://localhost:3001/callback")

(def oauth-path (:scauth-path config/environment))

(def oauth-authorisation-path (str (:scauth-path config/environment) "/authorisation?client_id=" client-id "&response_type=code&redirect_uri=" callback-uri ))

(def oauth-token-path (str (:scauth-path config/environment) "/token"))

(defn html-response [s]
  (-> s
      r/response
      (r/content-type "text/html")))

(defn logged-in? [request]
  (get-in request [:session :access-token]))

(defn home [request]
  (r/redirect (path :login)))

(defn show-login-form [request]
  (if (logged-in? request)
    (r/redirect (path :voting))
    (html-response (login/login-page request))))

(defn login [request]
  (let [response
        (-> (r/redirect oauth-authorisation-path)
            (assoc :params {:client_id client-id :response_type "code" :redirect_uri callback-uri})
            (assoc-in [:headers "accept"] "text/html"))]
    response))

(defn oauth-callback [request]
  (let [auth-code (get-in request [:params :code])
        token-response (http/get oauth-token-path {:query-params {:grant_type   "authorization_code"
                                                                  :redirect_uri callback-uri
                                                                  :code         auth-code
                                                                  :client_id     client-id
                                                                  :client_secret client-secret}})]
    (-> (r/redirect (path :voting))
        (assoc :session {:access-token (-> token-response
                                           :body
                                           (json/parse-string keyword)
                                           :access_token)}))))

(defn voting [request]
  (if (logged-in? request)
    (html-response (voting/voting-page request))
    (r/redirect (path :login))))

(defn show-poll-result [request]
  (html-response (view-poll/result-page request)))

(defn not-found [request]
  (html-response "404 PAGE NOT FOUND"))

(def handlers
  {:home                 home
   :show-login-form      show-login-form
   :login                login
   :oauth-callback       oauth-callback
   :voting               voting
   :show-poll-result     show-poll-result
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

