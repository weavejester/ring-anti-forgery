(ns ring.middleware.anti-forgery
  "Ring middleware to prevent CSRF attacks with an anti-forgery token."
  (:require [crypto.random :as random]))

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

(defn- secure-eql? [^String a ^String b]
  (if (and a b (= (.length a) (.length b)))
    (zero? (reduce bit-or
                   (map bit-xor (.getBytes a) (.getBytes b))))
    false))

(defn- extract-request-token [request]
  (-> request form-params (get "__anti-forgery-token")))

(defn- valid-request?
  "Indicates whether the supplied request has a valid token.
  The `extractor` is a function that accepts the request and
  extracts the token from it."
  [request extractor]
  (let [request-token (extractor request)
        stored-token (session-token request)]
    (and request-token
         stored-token
         (secure-eql? request-token stored-token))))

(defn- require-protection? [request]
  (some (hash-set (:request-method request)) [:post :put :delete]))

(defn access-denied-response
  "Produces an access-denied response with text/html content type, 403 status
  and the supplied body.

  The resulted response could be used to customize the access denied response
  in the wrap-anti-forgery middleware."
  [body]
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body body})

(def ^:private
  ^{:doc "defaults for wrap-anti-forgery"}
  defaults {:access-denied-response (access-denied-response
                                     "<h1>Invalid anti-forgery token</h1>")
            :request-token-extractor extract-request-token})

(defn wrap-anti-forgery
  "Middleware that prevents CSRF attacks. Any POST request to this handler must
  contain a '__anti-forgery-token' parameter equal to the last value of the
  *anti-request-forgery* var. If the token is missing or incorrect, an access-
  denied response is returned.

  The following options are available to customize the behavior of
  this middleware:
    :access-denied-response
      A custom ring response (map) such as produced by (access-denied-response).
      This response *should* have a status of 403.
    :request-token-extractor 
      In some cases you might want to pass the token not via the
      default request parameters but by other means (e.g. AngularJS
      passes the csrf token as a header). In this case you may supply
      a function that extracts the token from the request (or return
      nil of the token doesn't exist)."
  ([handler] (wrap-anti-forgery handler {}))
  ([handler options]
     (let [opts (merge defaults options)]
       (fn [request]
         (binding [*anti-forgery-token* (session-token request)]
           (if (and (require-protection? request)
                    (not (valid-request? request (:request-token-extractor opts))))
             (:access-denied-response opts)
             (if-let [response (handler request)]
               (assoc-session-token response request *anti-forgery-token*))))))))
