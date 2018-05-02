(ns jwt-stripe-app.core
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server)
  (:require [org.httpkit.client :as http]
		[clj-jwt.core  :refer :all]
		[clj-jwt.key   :refer [private-key]]
		[clj-time.core :refer [now plus days]]
		[aero.core :refer (read-config)])
  (:gen-class :main true))

(defn -main []
  (println "JWT Stripe App started"))
   
(def user_jwt "")
(def claim nil)

(defn read-secrets [] 
  (read-config "resources/secrets.edn"))
  
(defn build-claim [credentials]
  {:merchant_id (:merchant_id credentials)
   :subscription_id (:subscription_id credentials)
   :allowed_plans (:allowed_plans credentials)
   :exp (plus (now) (days 1))
   :iat (now)})
   
(defn update-user-token []
  (def secrets (read-secrets))
  (def claim (build-claim secrets))
  (let [token (-> claim jwt (sign :HS256 (:jwt_token secrets)) to-str)]
	(def user_jwt token)))

(defn login [req]
  (update-user-token)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str "JWT: " user_jwt)})
  
(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})
   
(defn client-test [req]
  (let [{:keys [status headers body error] :as resp} @(http/get "http://localhost:8888/")]
	{:status  200
	 :headers {"Content-Type" "text/html"}
	 :body    (str "http client used, result: " body)}))
  
(defn get-plan [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (-> user_jwt str->jwt :claims)})
  
(defroutes all-routes
  (GET "/" [] app)
  (GET "/test" [] client-test)
  (GET "/login" [] login)
  (GET "/plan" [] get-plan)
  (not-found "<p>Page not found.</p>"))
   
(run-server (site #'all-routes) {:port 8888})