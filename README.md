# ring-anti-forgery

This middleware prevents [CSRF][1] attacks by providing a randomly-generated
anti-forgery token.

## Install

Add the following dependency to your `project.clj`:

    [ring-anti-forgery "0.1.3"]

## Usage

When a handler is wrapped in the `wrap-anti-forgery` middleware, a randomly-
generated string is assigned to the `*anti-forgery-token*` var. This token must
be included as a parameter named "__anti-forgery-token" for all POST requests
to the handler. Typically you'll add this to a hidden input field:

    (str "<input type='hidden' name='__anti-forgery-token' value='" *anti-forgery-token* "'>")

A cookie of the same name is added to the response body by the middleware. If
the cookie and the POST parameter don't match, then a 403 Forbidden response
is returned. This ensures that requests cannot be POSTed from other domains.

## Caveats

The anti-forgery middleware will prevent POSTs working for web service routes,
so you should only apply this middleware to the part of your website meant
for browsers.

[1]: http://en.wikipedia.org/wiki/Cross-site_request_forgery
