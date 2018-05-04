(ns jwt-stripe-app.core-test
  (:use midje.sweet
		jwt-stripe-app.core) 
  (:require [jwt-stripe-app.core :as core]
			[clojure.string :as str]))

(defn build-login-body [] 
  (def login_body "{\"login\": \"merchant1\", \"password\": \"haslo\"}")
  (bytes (byte-array (map (comp byte int) login_body))))
			
(fact "get merchant id from JWT of merchant1 after login"
  ; Given
  (def token ((:headers (login (build-login-body))) "Set-Cookie"))
  ; When => Then
  (:merchant_id (get-credentials-from-token token)) => "merchant1")
  
(fact "clear JWT cookie after logout"
  ; When => Then
  ((:headers (logout nil)) "Set-Cookie") => "")
  
(fact "get subscription plan for username user"
  )
  
(fact "get allowed subscription plans for username user"
  )
  
(fact "update subscription plan for username user"
  )