(ns stonecutter-client.handler
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as r]
            [ring.adapter.jetty :refer [run-jetty]]
            [bidi.bidi :refer [path-for]]
            [environ.core :refer [env]]
            [scenic.routes :refer [scenic-handler]]
            [stonecutter-client.routes :refer [routes path]]
            [stonecutter-client.logging :as log-config]
            [clojure.tools.logging :as log]))

(defn html-response [s]
  (-> s
      r/response
      (r/content-type "text/html")))

(defn home [request]
 (html-response "Hello World!"))

(defn not-found [request]
  (html-response "404 PAGE NOT FOUND"))

(def handlers
  {:home          home})

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

