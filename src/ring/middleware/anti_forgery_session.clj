(ns ring.middleware.anti-forgery-session
  "Ring middleware to prevent CSRF attacks with up to limit anti-forgery tokens stored in the session."
  (:import java.security.SecureRandom
           sun.misc.BASE64Encoder))

(def ^:constant ^{:doc "Name of the hidden field in POST forms that contains the anti-forgery-token."}
  anti-forgery-token-name "__anti-forgery-token")

(def ^:private ^:dynamic ^{:doc "Private binding that stores the anti-forgery token for the current request."}
  *anti-forgery-token*)

(defn- generate-token []
  (let [seed (byte-array 32)]
    (.nextBytes (SecureRandom/getInstance "SHA1PRNG") seed)
    (.encode (BASE64Encoder.) seed)))

(defn anti-forgery-token
  "Returns the anti-forgery-token for the active request that
   must be included in POST forms if the handler is wrapped in
   wrap-anti-forgery-session."
  []
  (when-not @*anti-forgery-token*
    (reset! *anti-forgery-token* (generate-token)))
  @*anti-forgery-token*)

(defn- form-params [req]
  (merge (:form-params req)
         (:multipart-params req)))

(defn- valid-request? [req]
  (let [param-token  (-> req form-params (get anti-forgery-token-name))
        session-tokens (get-in req [:session anti-forgery-token-name])]
    (and param-token (some #(= param-token %) session-tokens))))

(defn- assoc-token-session [response request limit]
  (let [session (when (contains? response :session) (:session response {}))
        session (or session (:session request))
        session (update-in session [anti-forgery-token-name]
                           (fnil #(-> % (conj @*anti-forgery-token*) (subvec 1))
                                (vec (repeat limit nil))))]
    (assoc response :session session)))

(defn- post-request? [request]
  (= :post (:request-method request)))

(defn- access-denied [body]
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body body})

(defn wrap-anti-forgery-session
  "Middleware that prevents CSRF attacks. Any POST request to this handler must
  contain an anti-forgery-token-name parameter equal to one of the session's
  anti-forgery tokens. If the token is missing or incorrect, an access-
  denied response is returned.

  Takes limit, the number of tokens to store in the session."
  ([handler] (wrap-anti-forgery-session handler 1))
  ([handler limit]
      (fn [request]
        (binding [*anti-forgery-token* (atom nil)]
          (if (and (post-request? request) (not (valid-request? request)))
            (access-denied "<h1>Invalid anti-forgery token</h1>")
            (let [response (handler request)]
              (if (and response @*anti-forgery-token*)
                (assoc-token-session response request limit)
                response)))))))

