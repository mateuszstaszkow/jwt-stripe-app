(ns jwt-stripe-app-frontend.stripe-service
  (:use [clojure.string :only (join)]
		jwt-stripe-app-frontend.repository
		jwt-stripe-app-frontend.security)
  (:require [clj-jwt.core  :refer :all]
			[clj-jwt.key   :refer [private-key]]
			[clj-time.core :refer [now plus days]]
			[org.httpkit.client :as http]
			[clojure.data.json :as json]))

(def stripe_api_key "Bearer sk_test_BQokikJOvBiI2HlWgH4olfQ2")
(def stripe_api_url "https://api.stripe.com/v1/plans/")

(defn return-response [body]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    body})
   
(defn get-plan-details [plan_id]
  (let [stripe_url (str stripe_api_url plan_id)]
    (let [auth_headers {:headers {"Authorization" stripe_api_key}}]
      (let [{:keys [status headers body error] :as resp} @(http/get stripe_url auth_headers)]
        body))))
	
(defn update-plan-values [new_id, new_allowed_plans]
  (def updating_secrets (read-secrets))
  (if new_id
    (def updating_secrets (assoc updating_secrets :subscription_id new_id)))
  (if new_allowed_plans
    (def updating_secrets (assoc updating_secrets :allowed_plans new_allowed_plans)))
  updating_secrets)

(defn get-allowed-plans [headers]
  (let [credentials (get-credentials-from-cookie (headers "cookie"))]
    (let [plan_ids (:allowed_plans credentials)]
      (let [plan_bodies (map get-plan-details plan_ids)]
        (def plans_reponse "[")
        (doseq [x plan_bodies] (def plans_reponse (str plans_reponse x ",")))
        (def plans_reponse (str (join "" (drop-last plans_reponse)) "]"))
        (return-response plans_reponse)))))
  
(defn get-plan [headers]
  (let [credentials (get-credentials-from-cookie (headers "cookie"))]
    (let [plan_id (:subscription_id credentials)]
      (return-response (get-plan-details plan_id)))))
  
(defn update-plan [body]
  (let [dto (json/read-str (slurp body) :key-fn keyword)]
    (let [new_id (:subscription_id dto)]
      (let [new_allowed_plans (:allowed_plans dto)]
        (spit "resources/secrets.edn" (update-plan-values new_id new_allowed_plans))
        (return-response "")))))