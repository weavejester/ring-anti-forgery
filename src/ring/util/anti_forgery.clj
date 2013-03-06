(ns ring.util.anti-forgery
  (:use [hiccup core form def]
        ring.middleware.anti-forgery))

(defn anti-forgery-field
  "Create a hidden field with the session anti-forgery token as its value.
  This ensures that the form it's inside won't be stopped by the anti-forgery
  middleware."
  []
  (html (hidden-field "__anti-forgery-token" *anti-forgery-token*)))

(defhtml anti-forgery-meta
  "Create a pair of meta elements containing the token to allow easy access
from javasript. Uses the rails format for compatibility."
  []
  [:meta {:name "csrf-param" :content "anti-forgery-token"}]
  [:meta {:name "csrf-token" :content *anti-forgery-token*}])
