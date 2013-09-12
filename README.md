# Ring-Anti-Forgery

[![Build Status](https://secure.travis-ci.org/weavejester/ring-anti-forgery.png)](http://travis-ci.org/weavejester/ring-anti-forgery)

This middleware prevents [CSRF][1] attacks by providing a randomly-generated
anti-forgery token.

[1]: http://en.wikipedia.org/wiki/Cross-site_request_forgery

## Install

Add the following dependency to your `project.clj`:

    [ring-anti-forgery "0.3.0"]

## Usage

Apply the `wrap-anti-forgery` middleware to your Ring handler:

```clojure
(use 'ring.middleware.anti-forgery)

(def app
  (wrap-anti-forgery handler))
```

Any request that isn't a `HEAD` or `GET` request will now require an
anti-forgery token, or an "access denied" response will be returned.
The token is bound to the session, and accessible via the
`*anti-forgery-token*` var.

By default the middleware looks for the anti-forgery token in the
`__anti-forgery-token` form parameter, which can be added to your
forms as a hidden field. For convenience, this library provides a
function to generate the HTML of that hidden field:

```clojure
(use 'ring.util.anti-forgery)

(anti-forgery-field)  ;; returns the HTML for the anti-forgery field
```

The middleware also looks for the token in the `X-CSRF-Token` and
`X-XSRF-Token` header fields. This behavior can be customized further
using the `:read-token` option:

```clojure
(defn get-custom-token [request]
  (get-in request [:headers "x-forgery-token"]))

(def app
  (wrap-anti-forgery handler {:read-token get-custom-token}))
```

It's also possible to customize the error response returned when the
token is invalid or missing:

```clojure
(def custom-error-response
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body "<h1>Missing anti-forgery token</h1>"})

(def app
  (wrap-anti-forgery handler {:error-response custom-error-response}))
```

## Caveats

The anti-forgery middleware will prevent POSTs, PUTs, PATCHes, and
DELETEs, working for web service routes, so you should only apply this
middleware to the part of your website meant to be accessed by
browsers.

## License

Copyright © 2013 James Reeves

Distributed under the MIT License.
