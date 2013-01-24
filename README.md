# Ring-Anti-Forgery

[![Build Status](https://secure.travis-ci.org/weavejester/ring-anti-forgery.png)](http://travis-ci.org/weavejester/ring-anti-forgery)

This middleware prevents [CSRF][1] attacks by providing a randomly-generated
anti-forgery token.

## Install

Add the following dependency to your `project.clj`:

    [ring-anti-forgery "0.2.1"]

## Usage

When a handler is wrapped in the `wrap-anti-forgery` middleware, a randomly-
generated string is assigned to the `*anti-forgery-token*` var. This token must
be included as a parameter named "__anti-forgery-token" for all POST requests
to the handler, or as the value of the HTTP header X-Anti-Forgery-Token.

The ring-anti-forgery middleware includes a function to create a
hidden field that you can add to your forms:

```clojure
(use 'ring.util.anti-forgery)

(anti-forgery-field)   ;; returns a hidden field with the anti-forgery token
```

If you use XHR to perform requests from the client to your server, you typically
send JSON instead of form encoded data. In this scenario, you can set a header,
X-Anti-Forgery-Token. A common method for providing these values to your
JavaScript is via meta tags in `<head>`, and query the DOM for these values.

```clojure
[:meta {:name "csrf_header :content "X-Anti-Forgery-Token"}]
[:meta {:name "csrf_token" :content *anti-forgery-token*}]]
```

The forgery token is also automatically added as a session parameter
by the middleware. If the session parameter and the POST parameter
don't match, then a 403 Forbidden response is returned. This ensures
that requests cannot be POSTed from other domains.

## Caveats

The anti-forgery middleware will prevent POSTs working for web service routes,
so you should only apply this middleware to the part of your website meant
for browsers.

[1]: http://en.wikipedia.org/wiki/Cross-site_request_forgery

## License

Copyright © 2012 James Reeves

Distributed under the MIT License.
