(ns jwt-stripe-app-frontend.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
			  [ajax.core :refer [GET POST]]
			  [alandipert.storage-atom :refer [local-storage]]
			  [cljs.reader :as reader]))
			  
(enable-console-print!)

;; -------------------------
;; Store

(def empty_plan {"currency" "",
				 "product" "", 
				 "name" "", 
				 "interval" "", 
				 "interval_count" 0, 
				 "amount" 0})

(def new_plan nil)

(defonce app-state (local-storage (reagent/atom {:plan empty_plan :plans [empty_plan]}) :app-state))

;; -------------------------
;; Views

(declare log-in)
(declare page)
(declare home)

(defn plan-handler [res]
  (swap! app-state update-in [:plan] (fn [v] (reader/read-string (str res)))))
  
(defn plans-handler [res]
  (swap! app-state update-in [:plans] (fn [v] (reader/read-string (str res)))))
  
(defn reset-store []
  (reset! app-state {:plan empty_plan :plans [empty_plan]}))
  
(defn display-error-login [err]
  (js/alert "Wrong login or password!"))
  
(defn display-error [err]
  (js/alert "Something went wrong, please try again"))

(defn redirect-to-home [res]
  (reset-store)
  (reset! page #'home))
  
(defn input-element
  "An input element which updates its value on change"
  [id name type value]
  [:input {:id id
           :name name
           :class "form-control"
           :type type
           :required ""
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])
  
(defn login-page []
  (let [login (atom nil)]
   (fn []
    (let [password (atom nil)]
	  (fn []
	    [:div 
		  [:h2 "Please log in"]
		  [:div
			[:span "Login: "]
			(input-element "login" "login" "login" login)]
		  [:div 
			[:span "Password: "]
			(input-element "password" "password" "password" password)]
		  [:button {:on-click #(log-in @login @password)} "Log in"]])))))
  
(defn redirect-to-login-page [err]
  (reset! page #'login-page))
  
(defn logout []
  (POST "/logout" {:handler plan-handler})
  (redirect-to-login-page nil))
  
(defn load-plan-data []
  (if (= (@app-state :plan) empty_plan)
    (GET "/plan" {:handler plan-handler :error-handler redirect-to-login-page}))
  (if (= (@app-state :plans) [empty_plan])
    (GET "/plans" {:handler plans-handler :error-handler redirect-to-login-page})))
  
(defn refresh_plan [res]
  (plan-handler new_plan))
  
(defn update-plan [plan_data]
  (def new_plan plan_data)
  (POST "/plan"
     {:format :json
      :params {:subscription_id (plan_data "id")}
      :handler refresh_plan
      :error-handler refresh_plan}))
  
(defn plan-card [plan_data]
  [:div {:style {:border-style "solid"
				 :border-color "gray" 
				 :border-width 1
				 :border-radius 5
				 :margin 10
				 :padding 10
				 :cursor "pointer"}
		 :on-click #(update-plan plan_data)}
	[:div [:strong "Name: "] [:span (plan_data "name")]]
	[:div [:strong "Amount: "] [:span (str (plan_data "amount") " " (plan_data "currency"))]]
	[:div [:strong "Interval: "] [:span (plan_data "interval")]]
	[:div [:strong "Interval count: "] [:span (plan_data "interval_count")]]
	[:div [:strong "Product: "] [:span (plan_data "product")]]])
 
(defn home []
  (load-plan-data)
  [:div
    [:h2 "Current plan:"]
    (plan-card (@app-state :plan))
    [:h2 "Allowed plans:"]
    (for [allowed_plan (@app-state :plans)]
      (plan-card allowed_plan))
    [:button {:on-click #(logout)} "Logout"]])
   
(defn log-in [login password]
  (let [data {:login login :password password}]
   (POST "/login"
     {:format :json
      :params data
      :handler redirect-to-home
      :error-handler display-error-login}))
  (redirect-to-login-page nil))

;; -------------------------
;; Routes

(defonce page (atom #'home))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home))

(secretary/defroute "/login" []
  (reset! page #'login-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))