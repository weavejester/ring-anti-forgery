(ns ring.middleware.anti-forgery.session
  "Implements a synchronizer token pattern, see https://goo.gl/WRm7Kp"
  (:require [ring.middleware.anti-forgery.strategy :as strategy]
            [crypto.equality :as crypto]
            [crypto.random :as random]))

(defn- session-token [request]
  (get-in request [:session :ring.middleware.anti-forgery/anti-forgery-token]))

(deftype SessionStrategy []
  strategy/Strategy
  (get-token [this request]
    (or (session-token request)
        (random/base64 60)))

  (valid-token? [_ request token]
    (when-let [stored-token (session-token request)]
      (crypto/eq? token stored-token)))

  (write-token [this request response token]
    (let [old-token (session-token request)]
      (if (= old-token token)
        response
        (-> response
            (assoc :session (:session response (:session request)))
            (assoc-in
              [:session :ring.middleware.anti-forgery/anti-forgery-token]
              token))))))

(defn session-strategy []
  (->SessionStrategy))
