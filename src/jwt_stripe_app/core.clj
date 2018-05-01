(ns jwt-stripe-app.core
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server)
  (:gen-class :main true))

(defn -main []
  (println "JWT Stripe App started"))
  
(defn show-landing-page [req])
  
(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})
  
(defroutes all-routes
  (GET "/" [] show-landing-page)
  (GET "/app" [] app)     ; websocket
;  (context "/user/:id" []
;           (GET / [] get-user-by-id)
;           (POST / [] update-userinfo))
  (files "/static/") ; static file url prefix /static, in `public` folder
  (not-found "<p>Page not found.</p>")) ; all other, return 404
   
(run-server (site #'all-routes) {:port 8888})