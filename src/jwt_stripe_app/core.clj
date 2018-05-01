(ns jwt-stripe-app.core
  (:use org.httpkit.server)
  (:gen-class :main true))

(defn -main []
  (println "Hello World!"))
  
(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})
   
(run-server app {:port 8080})