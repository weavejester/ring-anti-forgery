(ns ring.middleware.anti-forgery
  "Ring middleware to prevent CSRF attacks with an anti-forgery token."
  (:import java.security.SecureRandom
           sun.misc.BASE64Encoder))

(def ^:dynamic ^{:doc "Binding that stores a anti-forgery token that must be included
  in POST forms if the handler is wrapped in wrap-anti-forgery."}
  *anti-forgery-token*)

(defn- generate-token []
  (let [seed (byte-array 64)]
    (.nextBytes (SecureRandom/getInstance "SHA1PRNG") seed)
    (.encode (BASE64Encoder.) seed)))

(defn- valid-request? [req]
  (let [param-token  (or (get-in req [:multipart-params "__anti-forgery-token"])
                         (get-in req [:form-params "__anti-forgery-token"]))
        cookie-token (get-in req [:cookies "__anti-forgery-token" :value])]
    (and param-token
         cookie-token
         (= param-token cookie-token))))

(defn- assoc-token-cookie [response]
  (assoc-in response [:cookies "__anti-forgery-token"] *anti-forgery-token*))

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
        (assoc-token-cookie (handler request))))))
