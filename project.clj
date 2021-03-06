(defproject stonecutter-client "0.1.0-SNAPSHOT"
  :description "A client app to test stonecutter with."
  :url "https://stonecutter-client.herokuapp.com"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-jetty-adapter "1.4.0-RC1"]
                 [scenic "0.2.3"]
                 [enlive "1.1.5"]
                 [hiccup "1.0.5"]
                 [clj-yaml "0.4.0"]
                 [clj-http "1.1.2"]
                 [cheshire "5.5.0"]
                 [environ "1.0.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojars.d-cent/stonecutter-oauth "0.2.0-SNAPSHOT"]
                 [clj-logging-config "1.9.12"]]
  :main stonecutter-client.handler
  :aot :all
  :profiles {:dev {:dependencies   [[ring-mock "0.1.5"]
                                    [midje "1.6.3"]
                                    [prone "0.8.2"]
                                    [kerodon "0.6.1"]]
                   :plugins        [[lein-ring "0.9.6"]
                                    [lein-midje "3.1.3"]
                                    [lein-ancient "0.6.7"]]
                   :ring {:handler stonecutter-client.handler/app
                          :init stonecutter-client.handler/lein-ring-init
                          :port 4000
                          :stacktrace-middleware prone.middleware/wrap-exceptions}
                   :resource-paths ["resources"]}})
