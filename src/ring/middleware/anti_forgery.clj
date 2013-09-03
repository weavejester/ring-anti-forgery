(ns ring.middleware.anti-forgery
  "Ring middleware to prevent CSRF attacks with an anti-forgery token."
  (:require [crypto.random :as random]
            [crypto.equality :as crypto]))

(def ^:dynamic
  ^{:doc "Binding that stores a anti-forgery token that must be included
          in POST forms if the handler is wrapped in wrap-anti-forgery."}
  *anti-forgery-token*)

(defn- session-token [request]
  (or (get-in request [:session "__anti-forgery-token"])
      (random/base64 60)))

(defn- assoc-session-token [response request token]
  (let [old-token (get-in request [:session "__anti-forgery-token"])]
    (if (= old-token token)
      response
      (-> response
          (assoc :session (:session request))
          (assoc-in [:session "__anti-forgery-token"] token)))))

(defn- form-params [request]
  (merge (:form-params request)
         (:multipart-params request)))

(defn- request-token [request]
  (or (-> request form-params (get "__anti-forgery-token"))
      (-> request :headers (get "x-csrf-token"))
      (-> request :headers (get "x-xsrf-token"))))

(defn- valid-request? [request]
  (let [user-token   (request-token request)
        stored-token (session-token request)]
    (and user-token
         stored-token
         (crypto/eq? user-token stored-token))))

(defn- get-request? [{method :request-method}]
  (or (= :head method)
      (= :get method)))

(defn- access-denied [body]
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body body})

(defn wrap-anti-forgery
  "Middleware that prevents CSRF attacks. Any POST request to the handler
  returned by this function must contain a valid anti-forgery token, or else an
  access-denied response is returned.

  The anti-forgery token can be placed into a HTML page via the
  *anti-forgery-token* var, which is bound to a random key unique to the
  current session. The token should be included in a form field named
  '__anti-forgery-token', or in the 'X-CSRF-Token' or 'X-XSRF-Token' headers."
  [handler]
  (fn [request]
    (binding [*anti-forgery-token* (session-token request)]
      (if (and (not (get-request? request)) (not (valid-request? request)))
        (access-denied "<h1>Invalid anti-forgery token</h1>")
        (if-let [response (handler request)]
          (assoc-session-token response request *anti-forgery-token*))))))
