(ns ring.middleware.anti-forgery.strategy)

(defprotocol Strategy
  "CSRF protection is based on the fact, that some state is embedded
  in the client webpage (e.g. as hidden form field)
  and the server is able to validate that state.

  OWASP documents a number of patterns how to create and validate that state
  in the form of a 'token', each with its own advantages and disadvantages.

  Strategy is the protocol to abstract the process
  of token creation and validation."
  (get-token [strategy request]
    "Returns a token to be used. Users of ring.middleware.anti-forgery should
     use the appropriate utility functions from `ring.util.anti-forgery`
     namespace.")

  (valid-token? [strategy request token]
    "Given the `request` and the `token` from that request, `valid-token?`
     returns true if the token is valid. Returns false otherwise.")

  (write-token [strategy request response token]
    "Some state management strategies do need to remember state (e.g., by
    storing it to some storage accessible in different requests). `write-token`
    is the method to handle state persistence, if necessary."))
