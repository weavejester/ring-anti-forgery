(ns ring.util.anti-forgery
  "Utility functions for inserting anti-forgery tokens into HTML forms."
  (:require [hiccup.core :as h]
            [hiccup.form :as hf]
            [ring.middleware.anti-forgery :as anti-forgery]))

(defn anti-forgery-token []
  (force anti-forgery/*anti-forgery-token*))

(defn anti-forgery-field
  "Create a hidden field with the session anti-forgery token as its value.
  This ensures that the form it's inside won't be stopped by the anti-forgery
  middleware."
  []
  (h/html (hf/hidden-field "__anti-forgery-token" (anti-forgery-token))))
