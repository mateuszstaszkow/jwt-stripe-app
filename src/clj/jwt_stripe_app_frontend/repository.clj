(ns jwt-stripe-app-frontend.repository
  (:require [aero.core :refer (read-config)]))

(defn read-secrets [] 
  (read-config "resources/secrets.edn"))