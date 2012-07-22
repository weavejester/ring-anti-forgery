(ns ring.middleware.test.anti-forgery-session
  (:use clojure.test)
  (:use ring.middleware.anti-forgery-session
        ring.mock.request))

(deftest forgery-protection-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery-session (constantly response))]
    (are [status req] (= (:status (handler req)) status)
      403 (request :post "/")
      403 (-> (request :post "/")
              (assoc :form-params {anti-forgery-token-name "foo"}))
      403 (-> (request :post "/")
              (assoc :session {anti-forgery-token-name ["foo"]})
              (assoc :form-params {anti-forgery-token-name "bar"}))
      200 (-> (request :post "/")
              (assoc :session {anti-forgery-token-name ["foo"]})
              (assoc :form-params {anti-forgery-token-name "foo"})))))

(deftest multipart-form-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery-session (constantly response))]
    (is (= (-> (request :post "/")
               (assoc :session {anti-forgery-token-name ["foo"]})
               (assoc :multipart-params {anti-forgery-token-name "foo"})
               handler
               :status)
           200))))

(deftest token-in-session-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler (fn [& _] (anti-forgery-token) response)
        handler  (wrap-anti-forgery-session handler)]
    (is (contains? (:session (handler (request :get "/")))
                         anti-forgery-token-name))
    (is (not= (get-in (handler (request :get "/"))
                      [:session anti-forgery-token-name])
              (get-in (handler (request :get "/"))
                      [:session anti-forgery-token-name])))))

(deftest token-binding-test
  (letfn [(handler [request]
            {:status 200
             :headers {}
             :body (anti-forgery-token)})]
    (let [response ((wrap-anti-forgery-session handler) (request :get "/"))]
      (is (= (get-in response [:session anti-forgery-token-name 0])
             (:body response))))))

(deftest nil-response
  (letfn [(handler [request] nil)]
    (let [response ((wrap-anti-forgery-session handler) (request :get "/"))]
      (is (nil? response)))))

(deftest no-lf-in-token-test
  (letfn [(handler [request]
            {:status 200
             :headers {}
             :body (anti-forgery-token)})]
    (let [response ((wrap-anti-forgery-session handler) (request :get "/"))
          token    (get-in response [:session anti-forgery-token-name 0])]
      (is (not (.contains token "\n"))))))

(deftest multiple-token-test
  (let [handler (fn [& _] ({:status 200 :headers {} :body (anti-forgery-token)}))
        resp (handler)
        handler (wrap-anti-forgery-session handler 2)
        resp1 (handler (request :get "/"))
        #_[resp1 resp2 resp3] #_(take 3 (repeatedly (handler (request :get "/"))))]
    (def r resp1)
    #_(is (= (:body resp1) (get-in resp1 [:session anti-forgery-token-name 2])))))

(deftest multiple-token-test
  (let [handler (fn [req] {:status 200 :headers {} :body (when (= (:request-method req) :get) (anti-forgery-token))})
        handler (wrap-anti-forgery-session handler 2)
        get-request (request :get "/")
        resp1 (handler get-request)
        resp2 (handler (assoc get-request :session (:session resp1)))
        resp3 (handler (assoc get-request :session (:session resp2)))]
    (are [resp tokens] (= (get-in resp [:session anti-forgery-token-name]) tokens)
         resp1 [nil (:body resp1)]
         resp2 [(:body resp1) (:body resp2)]
         resp3 [(:body resp2) (:body resp3)])
    (are [status req] (= (:status (handler req)) status)
         403 (-> (request :post "/")
                 (assoc :form-params {anti-forgery-token-name (:body resp1)} :session (:session resp3)))
         200 (-> (request :post "/")
                 (assoc :form-params {anti-forgery-token-name (:body resp2)} :session (:session resp3)))
         200 (-> (request :post "/")
                 (assoc :form-params {anti-forgery-token-name (:body resp3)} :session (:session resp3))))))