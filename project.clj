(defproject jwt-stripe-app "1.0.0-SNAPSHOT"
  :description "JWT, Stripe API, Web App"
  :profiles {:dev {:plugins [[lein-midje "3.2.1"]]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
				[http-kit "2.2.0"]
				[compojure "1.6.1"]
				[javax.servlet/servlet-api "2.5"]
				[aero "1.1.3"]
				[org.clojure/data.json "0.2.6"]
				[midje "1.9.1"]
				[clj-jwt "0.1.1"]]
  :main jwt-stripe-app.core)