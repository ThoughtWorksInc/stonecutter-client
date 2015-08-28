(ns stonecutter-client.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as r]
            [ring.adapter.jetty :refer [run-jetty]]
            [bidi.bidi :refer [path-for]]
            [environ.core :refer [env]]
            [scenic.routes :refer [scenic-handler]]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [stonecutter-oauth.client :as client]
            [stonecutter-oauth.jwt :as jwt]
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
(defn auth-jwks-url [] (get-env :auth-jwks-url (str (auth-url) "/api/jwk-set")))

(defn absolute-path [resource & params]
  (str (base-url) (apply path resource params)))

(def stonecutter-config-m {"oauth" (client/configure (auth-url)
                                                     (get-env :client-id)
                                                     (get-env :client-secret)
                                                     (str (base-url) "/oauth/callback"))
                           "openid" (client/configure (auth-url)
                                                      (get-env :client-id)
                                                      (get-env :client-secret)
                                                      (str (base-url) "/openid/callback")
                                                      :protocol :openid)})

(defn html-response [s]
  (-> s
      r/response
      (r/content-type "text/html")))

(defn logged-in? [request]
  (and (get-in request [:session :user])
       (get-in request [:session :access-token])))

(defn home [request]
  (let [protocol (get-in request [:route-params :protocol] "oauth")]
    (r/redirect (absolute-path :login :protocol protocol))))

(defn show-login-form [request]
  (let [protocol (get-in request [:route-params :protocol])]
    (if (logged-in? request)
      (r/redirect (absolute-path :voting :protocol protocol))
      (html-response (login/login-page request protocol)))))

(defn login [request]
  (let [protocol (get-in request [:route-params :protocol])]
    (client/authorisation-redirect-response (get stonecutter-config-m protocol))))

(defn logout [request]
  (let [protocol (get-in request [:route-params :protocol])]
    (-> (r/redirect (absolute-path :home-with-protocol :protocol protocol))
        (assoc :session nil))))

(defn logged-in-redirect [protocol access-token user-info] 
  (-> (r/redirect (absolute-path :voting :protocol protocol))
      (assoc :session {:access-token access-token 
                       :user (:email user-info)
                       :user-email-confirmed (:email_verified user-info)
                       :role (:role user-info)}))) 

(defn oauth-callback [request]
  (let [protocol "oauth" 
        stonecutter-config (get stonecutter-config-m protocol)] 
    (if-let [auth-code (get-in request [:params :code])]
      (let [token-response (client/request-access-token! stonecutter-config auth-code)
            access-token (:access_token token-response)
            user-info (:user-info token-response)]
        (logged-in-redirect protocol access-token user-info))
      (r/redirect (absolute-path :home)))))

(defn openid-callback [request]
  (let [protocol "openid"
        stonecutter-config (get stonecutter-config-m protocol)] 
    (if-let [auth-code (get-in request [:params :code])]
      (let [token-response (client/request-access-token! stonecutter-config auth-code)
            access-token (:access_token token-response)
            public-key-string (jwt/get-public-key-string-from-jwk-set-url (auth-jwks-url))
            user-info (jwt/decode stonecutter-config (:id_token token-response) public-key-string)]
        (logged-in-redirect protocol access-token user-info))
      (r/redirect (absolute-path :home)))))

(defn voting [request]
  (let [protocol (get-in request [:route-params :protocol])] 
    (if (logged-in? request)
      (html-response (voting/voting-page request))
      (r/redirect (absolute-path :login :protocol protocol)))))

(defn show-poll-result [request]
  (html-response (view-poll/result-page request)))

(defn not-found [request]
  (html-response "404 PAGE NOT FOUND"))

(def handlers
  {:home                 home
   :home-with-protocol   home
   :show-login-form      show-login-form
   :login                login
   :logout               logout
   :oauth-callback       oauth-callback
   :openid-callback      openid-callback
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
