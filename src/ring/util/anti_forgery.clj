(ns ring.util.anti-forgery
  (:use [hiccup core form element]
        ring.middleware.anti-forgery))

(defn anti-forgery-field
  "Create a hidden field with the session anti-forgery token as its value.
  This ensures that the form it's inside won't be stopped by the anti-forgery
  middleware."
  []
  (html (hidden-field "__anti-forgery-token" *anti-forgery-token*)))

(defn script-token
  "Generate a script tag defining the token as the given variable name in
  JavaScript or use the default, \"CSRF_TOKEN\"."
  [& [var-name]]
  (html (javascript-tag
          (str (or var-name "CSRF_TOKEN") " = \"" *anti-forgery-token* "\";"))))
