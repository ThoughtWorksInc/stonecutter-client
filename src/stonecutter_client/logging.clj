(ns stonecutter-client.logging
  (:require [clj-logging-config.log4j :as c]
            [clojure.tools.logging :as log]))

(defn init-logger! []
  (c/set-loggers!
    ["sandwich"]
    {:name    "logger"
     :out     (org.apache.log4j.ConsoleAppender.)
     :level   :debug
     :pattern "%d{yyyy-MM-dd HH:mm:ss} %-5p %c:%L - %m%n"}))
