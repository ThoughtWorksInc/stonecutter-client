(ns stonecutter-client.config
   (:require  [clojure.tools.logging :as log]))

(defn- env-lookup  [var-name]
    (get  (System/getenv) var-name))

(defn get-var 
    "Attempts to read an environment variable. If no variable is
    found will log a warning message and use the default. If no
      default is provided will use nil"
    ([var-name]
        (get-var var-name nil))
    ([var-name default] 
       (if-let  [variable  (get  (System/getenv) var-name)]
             variable
             (do
                     (if default
                                 (log/info  (str "Failed to look up environment variable \"" var-name "\" - using provided default"))
                                 (log/error  (str "Failed to look up environment variable \"" var-name "\" - no default provided"))) 
                     default))))

(def environment
   {:scauth-path (get-var "SCAUTH_PATH" "http://stonecutter.herokuapp.com")
    :client-id  (get-var "CLIENT_ID" "2VMA422KLEZJXRJZTR5A7BKKHSZ63PGM")
    :client-secret  (get-var "CLIENT_SECRET" "WGTJ6GMDZ52JG5ATOCA6G7PWZBEWPGYT")})

