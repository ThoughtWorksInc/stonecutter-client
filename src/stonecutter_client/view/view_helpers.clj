(ns stonecutter-client.view.view-helpers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.tools.logging :as log]))

(defn load-template [path]
  (log/debug (format "Loading template %s from file" path))
  (html/html-resource path))
