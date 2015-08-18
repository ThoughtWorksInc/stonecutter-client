(ns stonecutter-client.view.view-poll
  (:require [net.cgrand.enlive-html :as html]
            [stonecutter-client.view.view-helpers :as vh]))

(defn result-page [request]
  (apply str  
         (-> (vh/load-template "public/view-poll.html")
             html/emit*
             vh/set-static-paths)))


