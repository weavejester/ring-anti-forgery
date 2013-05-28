(ns ring.middleware.test.anti-forgery
  (:use clojure.test :reload)
  (:use ring.middleware.anti-forgery
        ring.mock.request))

(def param-name "authenticity-token")

(deftest forgery-protection-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (are [status req] (= (:status (handler req)) status)
      403 (request :post "/")
      403 (-> (request :post "/")
              (assoc :form-params {param-name "foo"}))
      403 (-> (request :post "/")
              (assoc :session {param-name "foo"})
              (assoc :form-params {param-name "bar"}))
      200 (-> (request :post "/")
              (assoc :session {param-name "foo"})
              (assoc :form-params {param-name "foo"})))))

(deftest forgery-protection-test-with-custom-param-name
  (let [response {:status 200, :headers {}, :body "Foo"}
        param-name "__anti-forgery-token"
        handler  (wrap-anti-forgery
                   (constantly response)
                   {:param-name param-name})]
    (are [status req] (= (:status (handler req)) status)
      403 (request :post "/")
      403 (-> (request :post "/")
              (assoc :form-params {param-name "foo"}))
      403 (-> (request :post "/")
              (assoc :session {param-name "foo"})
              (assoc :form-params {param-name "bar"}))
      200 (-> (request :post "/")
              (assoc :session {param-name "foo"})
              (assoc :form-params {param-name "foo"})))))


(deftest multipart-form-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (is (= (-> (request :post "/")
               (assoc :session {param-name "foo"})
               (assoc :multipart-params {param-name "foo"})
               handler
               :status)
           200))))

(deftest token-in-session-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (is (contains? (:session (handler (request :get "/")))
                   param-name))
    (is (not= (get-in (handler (request :get "/"))
                      [:session param-name])
              (get-in (handler (request :get "/"))
                      [:session param-name])))))

(deftest token-binding-test
  (letfn [(handler [request]
            {:status 200
             :headers {}
             :body *anti-forgery-token*})]
    (let [response ((wrap-anti-forgery handler) (request :get "/"))]
      (is (= (get-in response [:session param-name])
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
          token    (get-in response [:session param-name])]
      (is (not (.contains token "\n"))))))

(deftest single-token-per-session-test
  (let [expected {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly expected))
        actual   (handler
                  (-> (request :get "/")
                      (assoc-in [:session param-name] "foo")))]
    (is (= actual expected))))

(deftest not-overwrite-session-test
  (let [response {:status 200 :headers {} :body nil}
        handler  (wrap-anti-forgery (constantly response))
        session  (:session (handler (-> (request :get "/")
                                        (assoc-in [:session "foo"] "bar"))))]
    (is (contains? session param-name))
    (is (= (session "foo") "bar"))))
