(ns jwt-stripe-app-frontend.prod
  (:require [jwt-stripe-app-frontend.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
