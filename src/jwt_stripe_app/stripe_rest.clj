(ns jwt-stripe-app.stripe-rest
  (:use [compojure.route :only [not-found]]
        [compojure.core :only [defroutes GET POST]]
		jwt-stripe-app.stripe-service
		jwt-stripe-app.security))

(defn get-plan-auth [headers]
  (if (verify-token (headers "cookie"))
    (get-plan headers)
	{:status 401}))
  
(defn get-allowed-plans-auth [headers]
  (if (verify-token (headers "cookie"))
    (get-allowed-plans headers)
	{:status 401}))
  
(defn update-plan-auth [body headers]
  (if (verify-token (headers "cookie"))
    (update-plan body)
	{:status 401}))
  
(defroutes all-routes
  (POST "/login" {body :body} (login body))
  (POST "/logout" [] logout)
  (GET "/plan"  {headers :headers} (get-plan-auth headers))
  (POST "/plan" {body :body headers :headers} (update-plan-auth body headers))
  (GET "/plans" {headers :headers} (get-allowed-plans-auth headers))
  (not-found {:status 404}))