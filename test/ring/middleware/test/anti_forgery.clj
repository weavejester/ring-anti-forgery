(ns ring.middleware.test.anti-forgery
  (:use clojure.test :reload)
  (:use ring.middleware.anti-forgery
        ring.mock.request))

(deftest request-token-test
  (let [request-token-type-test (fn [set-request-token]
    (let [response {:status 200, :headers {}, :body "Foo"}
          handler  (wrap-anti-forgery (constantly response))]
      (are [status req] (= status (:status (handler req)))
        ; no token in session or request
        403 (request :post "/")
        ; send request token when none saved in session
        403 (-> (request :post "/")
                (set-request-token "foo"))
        ; send incorrect request token
        403 (-> (request :post "/")
                (assoc :session {"__anti-forgery-token" "foo"})
                (set-request-token "bar"))
        ; send correct request token
        200 (-> (request :post "/")
                (assoc :session {"__anti-forgery-token" "foo"})
                (set-request-token "foo")))))]
    (request-token-type-test
      (fn [request token] (assoc request
                                 :form-params {"__anti-forgery-token" token})))
    (request-token-type-test
      (fn [request token] (assoc request
                                 :multipart-params {"__anti-forgery-token" token})))
    (request-token-type-test
      (fn [request token] (assoc request
                                 :headers {"x-anti-forgery-token" token})))))

(deftest token-in-session-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (is (contains? (:session (handler (request :get "/")))
                   "__anti-forgery-token"))
    (is (not= (get-in (handler (request :get "/"))
                      [:session "__anti-forgery-token"])
              (get-in (handler (request :get "/"))
                      [:session "__anti-forgery-token"])))))

(deftest token-binding-test
  (letfn [(handler [request]
            {:status 200
             :headers {}
             :body *anti-forgery-token*})]
    (let [response ((wrap-anti-forgery handler) (request :get "/"))]
      (is (= (get-in response [:session "__anti-forgery-token"])
             (:body response))))))

(deftest nil-response-test
  (letfn [(handler [request] nil)]
    (let [response ((wrap-anti-forgery handler) (request :get "/"))]
      (is (nil? response)))))

(deftest no-lf-in-token-test
  (letfn [(handler [request]
            {:status 200
             :headers {}
             :body *anti-forgery-token*})]
    (let [response ((wrap-anti-forgery handler) (request :get "/"))
          token    (get-in response [:session "__anti-forgery-token"])]
      (is (not (.contains token "\n"))))))

(deftest single-token-per-session-test
  (let [expected {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly expected))
        actual   (handler
                  (-> (request :get "/")
                      (assoc-in [:session "__anti-forgery-token"] "foo")))]
    (is (= actual expected))))

(deftest not-overwrite-session-test
  (let [response {:status 200 :headers {} :body nil}
        handler  (wrap-anti-forgery (constantly response))
        session  (:session (handler (-> (request :get "/")
                                        (assoc-in [:session "foo"] "bar"))))]
    (is (contains? session "__anti-forgery-token"))
    (is (= (session "foo") "bar"))))
