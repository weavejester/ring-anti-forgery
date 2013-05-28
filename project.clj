(defproject org.clojars.bluemont/ring-anti-forgery "0.2.2-SNAPSHOT"
  :description "Ring middleware to prevent CSRF attacks"
  :url "https://github.com/weavejester/ring-anti-forgery"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [crypto-random "1.1.0"]
                 [hiccup "1.0.0"]]
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.1"]]}
   :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}})
