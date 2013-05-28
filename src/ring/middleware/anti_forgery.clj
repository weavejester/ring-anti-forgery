(ns ring.middleware.anti-forgery
  "Ring middleware to prevent CSRF attacks with an anti-forgery token."
  (:require [crypto.random :as random]))

(def ^:dynamic
  ^{:doc "Binding that stores a anti-forgery token that must be included
          in POST forms if the handler is wrapped in wrap-anti-forgery."}
  *anti-forgery-token*)

(defn- session-token
  "Returns token from request if present; otherwise, generates new token."
  [request name]
  (or (get-in request [:session name])
      (random/base64 60)))

(defn- assoc-session-token
  "Return updated response with `name` param set to `token` value."
  [response request name token]
  (let [old-token (get-in request [:session name])]
    (if (= old-token token)
      response
      (-> response
          (assoc :session (:session request))
          (assoc-in [:session name] token)))))

(defn- form-params
  ""
  [request]
  (merge (:form-params request)
         (:multipart-params request)))

(defn- secure-eql?
  "TODO: Explain why a plain = is not good enough."
  [^String a ^String b]
  (if (and a b (= (.length a) (.length b)))
    (zero? (reduce bit-or (map bit-xor (.getBytes a) (.getBytes b))))
    false))

(defn- valid-request?
  "Is the request valid?"
  [request name]
  (let [param-token (-> request form-params (get name))
        stored-token (session-token request name)]
    (and param-token stored-token (secure-eql? param-token stored-token))))

(defn- post-request?
  ""
  [request]
  (= :post (:request-method request)))

(defn- access-denied
  ""
  [body]
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body body})

(def ^:private
  ^{:doc "defaults for wrap-anti-forgery"}
  defaults
  {:param-name "authenticity-token"})

(defn wrap-anti-forgery
  "Middleware that prevents CSRF attacks. Any POST request to this handler must
  contain a parameter (named 'authenticity-token' by default) equal to the last
  last value of the *anti-request-forgery* var. If the token is missing or
  incorrect, an access-denied response is returned.

  The following options are available to customize the behavior:
    * :param-name (a string)"
  ([handler] (wrap-anti-forgery handler {}))
  ([handler options]
   (let [opts (merge defaults options)
         name (opts :param-name)]
     (fn [request]
       (binding [*anti-forgery-token* (session-token request name)]
         (if (and (post-request? request)
                  (not (valid-request? request name)))
           (access-denied "<h1>Invalid anti-forgery token</h1>")
           (if-let [response (handler request)]
             (assoc-session-token
               response request name *anti-forgery-token*))))))))
