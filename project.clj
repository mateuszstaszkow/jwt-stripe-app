(defproject jwt-stripe-app "1.0.0-SNAPSHOT"
  :description "JWT, Stripe API, Web App"
  :dependencies [[org.clojure/clojure "1.8.0"]
				[http-kit "2.2.0"]
				[compojure "1.6.1"]
				[javax.servlet/servlet-api "2.5"]
				[aero "1.1.3"]
				[org.clojure/data.json "0.2.6"]
				[clj-jwt "0.1.1"]]
  :main jwt-stripe-app.core)