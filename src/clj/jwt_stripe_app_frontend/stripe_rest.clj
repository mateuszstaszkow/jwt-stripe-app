(ns jwt-stripe-app-frontend.stripe-rest
  (:use jwt-stripe-app-frontend.stripe-service
		jwt-stripe-app-frontend.security)
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found]]
            [hiccup.page :refer [include-js include-css html5]]
            [jwt-stripe-app-frontend.middleware :refer [wrap-middleware]]
			[ring.middleware.defaults :refer :all]
            [config.core :refer [env]]))
			
(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))
		
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
  (GET "/" [] (loading-page))
  (GET "/login" [] (loading-page))
  (POST "/login" {body :body} (login body))
  (POST "/logout" [] logout)
  (GET "/plan"  {headers :headers} (get-plan-auth headers))
  (POST "/plan" {body :body headers :headers} (update-plan-auth body headers))
  (GET "/plans" {headers :headers} (get-allowed-plans-auth headers))
  (not-found {:status 404}))
  
(def app (wrap-defaults #'all-routes (assoc-in site-defaults [:security :anti-forgery] false)))

;(def app (wrap-defaults app-routes ))