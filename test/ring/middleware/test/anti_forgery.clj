(ns ring.middleware.test.anti-forgery
  (:use clojure.test :reload)
  (:use ring.middleware.anti-forgery
        ring.mock.request))

(deftest forgery-protection-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (are [status req] (= (:status (handler req)) status)
      403 (request :post "/")
      403 (-> (request :post "/")
              (assoc :form-params {"__anti-forgery-token" "foo"}))
      403 (-> (request :post "/")
              (assoc :session {"__anti-forgery-token" "foo"})
              (assoc :form-params {"__anti-forgery-token" "bar"}))
      200 (-> (request :post "/")
              (assoc :session {"__anti-forgery-token" "foo"})
              (assoc :form-params {"__anti-forgery-token" "foo"})))))

(deftest multipart-form-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (is (= (-> (request :post "/")
               (assoc :session {"__anti-forgery-token" "foo"})
               (assoc :multipart-params {"__anti-forgery-token" "foo"})
               handler
               :status)
           200))))

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

(deftest access-denied-response-test
  (let [response (access-denied-response "some body")]
    (is (= (:status response) 403) "default status")
    (is (= (:body response) "some body"))))

(deftest wrap-anti-forgery-access-denied-response-test
  (testing "default response"
    (let [handler (wrap-anti-forgery (constantly "some response"))
          expected (access-denied-response "<h1>Invalid anti-forgery token</h1>")]
      (is (= (handler (request :post "/")) expected))))
  (testing "custom response"
    (let [expected-response (access-denied-response "custom body")
          handler (wrap-anti-forgery (constantly "some response")
                                     {:access-denied-response expected-response})]
      (is (= (handler (request :post "/")) expected-response)))))
