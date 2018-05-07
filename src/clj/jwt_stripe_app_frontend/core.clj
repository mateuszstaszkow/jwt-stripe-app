(ns jwt-stripe-app-frontend.core
  (:use [compojure.handler :only [site]]
        org.httpkit.server
		jwt-stripe-app-frontend.security)
  (:require [jwt-stripe-app-frontend.stripe-rest :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-jetty app {:port port :join? false})))