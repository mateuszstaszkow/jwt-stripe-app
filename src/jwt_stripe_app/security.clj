(ns jwt-stripe-app.security
  (:use jwt-stripe-app.repository
		bcrypt-clj.auth)
  (:require [clj-jwt.core  :refer :all]
			[clj-jwt.key   :refer [private-key]]
			[clj-time.core :refer [now plus days]]
			[clojure.data.json :as json]))

(def jwt_secret "tajny-klucz")

(def user_jwt "")
(def claim nil)

(defn get-credentials-from-token [token]
   (-> token str->jwt :claims))
   
(defn build-claim [credentials]
  {:merchant_id (:merchant_id credentials)
   :subscription_id (:subscription_id credentials)
   :allowed_plans (:allowed_plans credentials)
   :exp (plus (now) (days 1))
   :iat (now)})
   
(defn update-user-token []
  (let [secrets (read-secrets)]
    (let [claim (build-claim secrets)]
      (let [token (-> claim jwt (sign :HS256 jwt_secret) to-str)]
	    (def user_jwt token)))))
		
(defn verify-token [token]
  (and (and (not= token nil) (not= token ""))
    (= (str (:merchant_id (read-secrets)))
	  (:merchant_id (get-credentials-from-token token)))))
	  
(defn update-and-set-token []
  (update-user-token)
  {:status  200
   :headers {"Set-Cookie" user_jwt}})

(defn login [body]
  (let [dto (json/read-str (slurp body) :key-fn keyword)]
    (let [secrets (read-secrets)]
      (let [equal_login (= (:login dto) (str (:merchant_id secrets)))]
        (let [equal_password (check-password (:password dto) (str (:password secrets)))]
          (if (and equal_login equal_password)
            (update-and-set-token)
	        {:status 401}))))))
   
(defn logout [req]
  {:status  200
   :headers {"Set-Cookie" ""}})
			

	
