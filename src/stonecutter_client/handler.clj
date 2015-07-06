(ns stonecutter-client.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as r]
            [ring.adapter.jetty :refer [run-jetty]]
            [bidi.bidi :refer [path-for]]
            [environ.core :refer [env]]
            [scenic.routes :refer [scenic-handler]]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [stonecutter-client.logging :as log-config]
            [stonecutter-client.routes :refer [routes path]]
            [stonecutter-client.view.login :as login]
            [stonecutter-client.view.voting :as voting]
            [stonecutter-client.view.view-poll :as view-poll]))

(defn get-env
  ([k]
   (get-env k nil))
  ([k default]
   (if-let [value (env k)]
     value
     (do
       (if default
         (log/info (str "Using default environment variable for: " k))
         (log/warn (str "Missing environment variable for: " k)))
       default))))

(defn base-url [] (get-env :base-url "http://localhost:4000"))

(defn auth-url [] (get-env :auth-url "http://localhost:3000"))

(defn absolute-path [resource]
  (str (base-url) (path resource)))

(defn html-response [s]
  (-> s
      r/response
      (r/content-type "text/html")))

(defn logged-in? [request]
  (and (get-in request [:session :user])
       (get-in request [:session :access-token])))

(defn home [request]
  (r/redirect (absolute-path :login)))

(defn show-login-form [request]
  (if (logged-in? request)
    (r/redirect (absolute-path :voting))
    (html-response (login/login-page request))))

(defn login [request]
  (let [client-id (get-env :client-id)
        callback-uri (str (base-url) "/callback")
        oauth-authorisation-path (str (auth-url) "/authorisation?client_id=" client-id "&response_type=code&redirect_uri=" callback-uri)
        response
        (-> (r/redirect oauth-authorisation-path)
            (assoc :params {:client_id client-id :response_type "code" :redirect_uri callback-uri})
            (assoc-in [:headers "accept"] "text/html"))]
    response))

(defn oauth-callback [request]
  (let [client-id (get-env :client-id)
        client-secret (get-env :client-secret)
        callback-uri (str (base-url) "/callback")
        oauth-token-path (str (auth-url) "/api/token")
        auth-code (get-in request [:params :code])
        token-response (http/post oauth-token-path {:form-params {:grant_type    "authorization_code"
                                                                  :redirect_uri  callback-uri
                                                                  :code          auth-code
                                                                  :client_id     client-id
                                                                  :client_secret client-secret}})
        token-body (-> token-response
                       :body
                       (json/parse-string keyword))]
    (-> (r/redirect (absolute-path :voting))
        (assoc :session {:access-token (:access_token token-body)
                         :user (:user-email token-body)}))))

(defn voting [request]
  (if (logged-in? request)
    (html-response (voting/voting-page request))
    (r/redirect (absolute-path :login))))

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
   :show-poll-result     show-poll-result})

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

(defn -main [& args]
  (log-config/init-logger!)
  (-> app wrap-error-handling (run-jetty {:port (Integer. (get-env :port "4000"))})))

(defn lein-ring-init
  "Called once when running lein ring server"
  []
  (log-config/init-logger!))
