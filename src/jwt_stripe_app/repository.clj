(ns jwt-stripe-app.repository
  (:require [aero.core :refer (read-config)]))

(defn read-secrets [] 
  (read-config "resources/secrets.edn"))