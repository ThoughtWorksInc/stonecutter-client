(ns stonecutter-client.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as r]
            [ring.adapter.jetty :refer [run-jetty]]
            [bidi.bidi :refer [path-for]]
            [environ.core :refer [env]]
            [scenic.routes :refer [scenic-handler]]
            [clojure.tools.logging :as log]
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

(defn default-json-web-key-string [] (slurp "./resources/test-key.json"))
(defn public-key [] (jwt/json->key-pair (get-env :json-web-key (default-json-web-key-string))))

(defn absolute-path [resource & params]
  (str (base-url) (apply path resource params)))

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

(def stonecutter-config-m {"oauth" (client/configure (auth-url)
                                                     (get-env :client-id)
                                                     (get-env :client-secret)
                                                     (str (base-url) "/oauth/callback"))
                           "openid" (client/configure (auth-url)
                                                      (get-env :client-id)
                                                      (get-env :client-secret)
                                                      (str (base-url) "/openid/callback")
                                                      :protocol :openid
                                                      :public-key (public-key))})

(defn login [request]
  (let [protocol (get-in request [:route-params :protocol])]
    (client/authorisation-redirect-response (get stonecutter-config-m protocol))))

(defn logout [request]
  (let [protocol (get-in request [:route-params :protocol])]
    (-> (r/redirect (absolute-path :home-with-protocol :protocol protocol))
        (assoc :session nil))))

(defn oauth-callback [request]
  (let [protocol (get-in request [:route-params :protocol])
        stonecutter-config (get stonecutter-config-m protocol)] 
    (if-let [auth-code (get-in request [:params :code])]
      (let [token-response (client/request-access-token! stonecutter-config auth-code)
            user-info (if (= protocol "openid")
                        (jwt/decode (:public-key stonecutter-config)
                                    (:client-id stonecutter-config)
                                    (:auth-provider-url stonecutter-config)
                                    (:id_token token-response))
                        (:user-info token-response))]
        (-> (r/redirect (absolute-path :voting :protocol protocol))
            (assoc :session {:access-token (:access_token token-response)
                             :user (:email user-info)
                             :user-email-confirmed (:email_verified user-info)
                             :role (:role user-info)})))
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
