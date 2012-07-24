(ns ring.middleware.anti-forgery
  "Ring middleware to prevent CSRF attacks with an anti-forgery token."
  (:require [crypto.random :as random]))

(def ^:dynamic
  ^{:doc "Binding that stores a anti-forgery token that must be included
          in POST forms if the handler is wrapped in wrap-anti-forgery."}
  *anti-forgery-token*)

(defn- generate-token []
  (random/base64 32))

(defn- form-params [request]
  (merge (:form-params request)
         (:multipart-params request)))

(defn- valid-request? [request]
  (let [param-token   (-> request form-params (get "__anti-forgery-token"))
        session-token (get-in request [:session "__anti-forgery-token"])]
    (and param-token
         session-token
         (= param-token session-token))))

(defn- assoc-session-token [response]
  (assoc-in response [:session "__anti-forgery-token"] *anti-forgery-token*))

(defn- post-request? [request]
  (= :post (:request-method request)))

(defn- access-denied [body]
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body body})

(defn wrap-anti-forgery
  "Middleware that prevents CSRF attacks. Any POST request to this handler must
  contain a '__anti-forgery-token' parameter equal to the last value of the
  *anti-request-forgery* var. If the token is missing or incorrect, an access-
  denied response is returned."
  [handler]
  (fn [request]
    (binding [*anti-forgery-token* (generate-token)]
      (if (and (post-request? request) (not (valid-request? request)))
        (access-denied "<h1>Invalid anti-forgery token</h1>")
        (if-let [response (handler request)]
          (assoc-session-token response))))))
