(ns jwt-stripe-app.core-test
  (:use midje.sweet
		jwt-stripe-app.core) 
  (:require [jwt-stripe-app.core :as core]
			[clojure.string :as str]))

(defn build-login-body [] 
  (def login_body "{\"login\": \"merchant1\", \"password\": \"haslo\"}")
  (bytes (byte-array (map (comp byte int) login_body))))
  
(defn build-auth-headers []
  {"cookie" ((:headers (login (build-login-body))) "Set-Cookie")})
			
(fact "get merchant id from JWT of merchant1 after login"
  ; Given
  (def login_body (build-login-body))
  ; When
  (def token ((:headers (login login_body)) "Set-Cookie"))
  ; Then
  (:merchant_id (get-credentials-from-token token)) => "merchant1")
  
(fact "get not authorized status for plan endpoint"
  ; Given
  (def headers {"cookie" ""})
  ; When
  (def response (get-plan-auth headers))
  ; Then
  (:status response) => 401)
  
(fact "get not authorized status for allowed plans endpoint"
  ; Given
  (def headers {"cookie" ""})
  ; When
  (def response (get-allowed-plans-auth headers))
  ; Then
  (:status response) => 401)
  
(fact "get not authorized status for update plan endpoint"
  ; Given
  (def headers {"cookie" ""})
  ; When
  (def response (update-plan-auth nil headers))
  ; Then
  (:status response) => 401)
  
(fact "clear JWT cookie after logout"
  ; When
  (def token ((:headers (logout nil)) "Set-Cookie"))
  ; Then
  token => "")
  
(fact "get subscription plan for user merchant1"
  ; Given
  (def headers (build-auth-headers))
  ; When
  (def response (get-plan-auth headers))
  ; Then
  (:status response) => 200)
  
(fact "get allowed subscription plans for user merchant1"
  ; Given
  (def headers (build-auth-headers))
  ; When
  (def response (get-allowed-plans-auth headers))
  ; Then
  (:status response) => 200)
  
(fact "update subscription plan for user merchant1"
  ; Given
  (def headers (build-auth-headers))
  (def update_dto_bytes (bytes (byte-array (map (comp byte int) "{}"))))
  ; When
  (def response (get-allowed-plans-auth headers))
  ; Then
  (:status response) => 200)