(defproject ring-anti-forgery "0.2.0-SNAPSHOT"
  :description "Ring middleware to prevent CSRF attacks"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [crypto-random "1.1.0"]]
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.1"]]}})
