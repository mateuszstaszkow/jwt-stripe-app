(ns jwt-stripe-app.core
  (:use [compojure.handler :only [site]]
        org.httpkit.server
		jwt-stripe-app.security
		jwt-stripe-app.stripe-rest)
  (:gen-class :main true))

(defn -main []
  (println "JWT Stripe App started"))
   
(run-server (site #'all-routes) {:port 8888})