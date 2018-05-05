(ns ^:figwheel-no-load jwt-stripe-app-frontend.dev
  (:require
    [jwt-stripe-app-frontend.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
