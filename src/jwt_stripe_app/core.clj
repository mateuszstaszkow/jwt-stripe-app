(ns jwt-stripe-app.core
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
		[clojure.string :only (join)]
        org.httpkit.server
		bcrypt-clj.auth)
  (:require [org.httpkit.client :as http]
		[clj-jwt.core  :refer :all]
		[clj-jwt.key   :refer [private-key]]
		[clj-time.core :refer [now plus days]]
		[clojure.data.json :as json]
		[aero.core :refer (read-config)])
  (:gen-class :main true))

(def stripe_api_key "Bearer sk_test_BQokikJOvBiI2HlWgH4olfQ2")
(def stripe_api_url "https://api.stripe.com/v1/plans/")
(def jwt_secret "tajny-klucz")

(def user_jwt "")
(def claim nil)

(defn -main []
  (println "JWT Stripe App started"))

(defn read-secrets [] 
  (read-config "resources/secrets.edn"))
  
(defn build-claim [credentials]
  {:merchant_id (:merchant_id credentials)
   :subscription_id (:subscription_id credentials)
   :allowed_plans (:allowed_plans credentials)
   :exp (plus (now) (days 1))
   :iat (now)})
   
(defn get-credentials-from-token [token]
   (-> token str->jwt :claims))
   
(defn verify-token [token]
  (and (and (not= token nil) (not= token ""))
    (= (str (:merchant_id (read-secrets)))
	  (:merchant_id (get-credentials-from-token token)))))
   
(defn update-user-token []
  (def secrets (read-secrets))
  (def claim (build-claim secrets))
  (let [token (-> claim jwt (sign :HS256 jwt_secret) to-str)]
	(def user_jwt token)))
	
(defn update-and-set-token []
  (update-user-token)
  {:status  200
   :headers {"Set-Cookie" user_jwt}})

(defn convert-json-body-to-dto [body]
  (json/read-str (slurp body) :key-fn keyword))
   
(defn login [body]
  (def dto (convert-json-body-to-dto body))
  (def secrets (read-secrets))
  (def equal_login (= (:login dto) (str (:merchant_id secrets))))
  (def equal_password (check-password (:password dto) (str (:password secrets))))
  (if (and equal_login equal_password)
    (update-and-set-token)
	{:status 401}))
   
(defn logout [req]
  {:status  200
   :headers {"Set-Cookie" ""}})
  
(defn return-response [body]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    body})
  
(defn get-plan-details [plan_id]
  (def stripe_url (str stripe_api_url plan_id))
  (def auth_headers {:headers {"Authorization" stripe_api_key}})
  (let [{:keys [status headers body error] :as resp} @(http/get stripe_url auth_headers)]
    body))
  
(defn get-plan [headers]
  (def credentials (get-credentials-from-token (headers "cookie")))
  (def plan_id (:subscription_id credentials))
  (return-response (get-plan-details plan_id)))
  
(defn get-plan-auth [headers]
  (if (verify-token (headers "cookie"))
    (get-plan headers)
	{:status 401}))
  
(defn get-allowed-plans [headers]
  (def credentials (get-credentials-from-token (headers "cookie")))
  (def plan_ids (:allowed_plans credentials))
  (def plan_bodies (map get-plan-details plan_ids))
  (def plans_reponse "[")
  (doseq [x plan_bodies] (def plans_reponse (str plans_reponse x ",")))
  (def plans_reponse (str (join "" (drop-last plans_reponse)) "]"))
  (return-response plans_reponse))
  
(defn get-allowed-plans-auth [headers]
  (if (verify-token (headers "cookie"))
    (get-allowed-plans headers)
	{:status 401}))
  
(defn update-plan-values [new_id, new_allowed_plans]
  (def secrets (read-secrets))
  (if new_id
    (def secrets (assoc secrets :subscription_id new_id)))
  (if new_allowed_plans
    (def secrets (assoc secrets :allowed_plans new_allowed_plans)))
  secrets)
  
(defn update-plan [body]
  (def dto (convert-json-body-to-dto body))
  (def new_id (:subscription_id dto))
  (def new_allowed_plans (:allowed_plans dto))
  (spit "resources/secrets.edn" (update-plan-values new_id new_allowed_plans))
  (return-response ""))
  
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
  (not-found "<p>Page not found.</p>"))
   
(run-server (site #'all-routes) {:port 8888})