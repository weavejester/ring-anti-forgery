# Ring-Anti-Forgery

**Warning: This fork is a shot of generalizing this library so it'll be possible
to customize the way it handles CSRF protection. It's not yet ready!**

[![Build Status](https://secure.travis-ci.org/weavejester/ring-anti-forgery.png)](http://travis-ci.org/weavejester/ring-anti-forgery)

This middleware prevents [CSRF][1] attacks by providing a randomly-generated
anti-forgery token.

## Install

Add the following dependency to your `project.clj`:

    [ring-anti-forgery "0.3.0-SNAPSHOT"]

## Usage

### Default Behavior

When a handler is wrapped in the `wrap-anti-forgery` middleware, a
randomly- generated string is assigned to the `*anti-forgery-token*`
var. This token must be included as a parameter named
"__anti-forgery-token" for all POST/PUT/DELETE requests to the
handler.

The ring-anti-forgery middleware includes a function to create a
hidden field that you can add to your forms:

```clojure
(use 'ring.util.anti-forgery)

(anti-forgery-field)   ;; returns a hidden field with the anti-forgery token
```

The forgery token is also automatically added as a session parameter
by the middleware. If the session parameter and the POST parameter
don't match, then a 403 Forbidden response is returned. This ensures
that requests cannot be POSTed from other domains.

### Customizing token passing and response

Sometimes you might not want to use the default "__anti-forgery-token"
form parameter or you may want to customize the *access denied*
response. Here's an example for working with [AngularJS][2].

AngularJS uses a [different mechanism for working with csrf][3]. It
gets the token from the server via a cookie named *XSRF-TOKEN* and it
adds the token to all *post* requests via the *x-xsrf-token* header.
Here's how to use it.

A wrapper to add the required cookie. You can use it to wrap your
*home* page response:

```clojure
(ns some.ns
  (:use ring.middleware.anti-forgery
        [ring.util.codec :only (url-decode)]))

;; ...

(defn anti-forgery-cookie
  "wraps a response with"
  [response]
  (-> response
      (assoc-in [:cookies "XSRF-TOKEN" :value] *anti-forgery-token*)))

;; ...
```
Now you need to provide a custom token extractor and maybe a custom
*access denied* json response:

```clojure
;; ...

(def access-denied-response
  {:status 403,
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body "{\"error\": \"invalid token\"}"})

(defn token-header-extractor [request]
  (if-let [token (get-in request [:headers "x-xsrf-token"])]
    (url-decode token)))

;; now call wrap-anti-forgery with options:
;; ...
(-> handler
    (wrap-anti-forgery {:access-denied-response access-denied-response
                        :request-token-extractor token-header-extractor})
;; ...
```


## Caveats

The anti-forgery middleware will prevent POSTs working for web service routes,
so you should only apply this middleware to the part of your website meant
for browsers.

[1]: http://en.wikipedia.org/wiki/Cross-site_request_forgery
[2]: http://angularjs.org
[3]: http://docs.angularjs.org/api/ng.$http

## License

Copyright © 2012 James Reeves

Distributed under the MIT License.
