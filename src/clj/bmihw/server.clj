(ns bmihw.server
  (:require [bmihw.handler :refer [app]]
            [bmihw.firebase :as fb]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (fb/crackle)
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-jetty app {:port port :join? false})))
