(ns ring.middleware.anti-forgery.strategy
  "A namespace for containing the Strategy protocol.")

(defprotocol Strategy
  "Defines a strategy for protecting Ring handlers from CSRF attacks. "
  (valid-token? [strategy request token]
    "Given a request map and a token read from the request, return true if the
    request is valid, false otherwise.")

  (get-token [strategy request]
    "Returns a token to be used by the handler for a given request. The return
    value will be used in ring.middleware.anti-forgery/*anti-forgery-token*.")

  (write-token [strategy request response token]
    "Write the token to the response, if necessary. Some strategies require
    state to be stored in the session or in a cookie."))
