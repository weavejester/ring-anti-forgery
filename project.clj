(defproject ring/ring-anti-forgery "0.3.0"
  :description "Ring middleware to prevent CSRF attacks"
  :url "https://github.com/ring-clojure/ring-anti-forgery"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [crypto-random "1.1.0"]
                 [crypto-equality "0.1.0"]
                 [hiccup "1.0.0"]]
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.1"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}})
