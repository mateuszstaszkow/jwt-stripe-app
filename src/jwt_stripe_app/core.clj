(ns jwt-stripe-app.core
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server)
  (:require [org.httpkit.client :as http]
		[clj-jwt.core  :refer :all]
		[clj-jwt.key   :refer [private-key]]
		[clj-time.core :refer [now plus days]])
  (:gen-class :main true))

(defn -main []
  (println "JWT Stripe App started"))
   
(def user_jwt "")
(def claim nil)
(def merchant_id 1)
(def subscription_id 1234)
(def allowed_plans [1 2 3])
  
(defn show-landing-page [req])

(defn login [req]
  (def claim
    {:merchant_id merchant_id
	 :subscription_id subscription_id
	 :allowed_plans allowed_plans
     :exp (plus (now) (days 1))
     :iat (now)})
  (let [token (-> claim jwt (sign :HS256 "tajny-klucz") to-str)]
	(def user_jwt token))
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