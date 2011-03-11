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
              (assoc :cookies {"__anti-forgery-token" {:value "foo"}})
              (assoc :form-params {"__anti-forgery-token" "bar"}))
      200 (-> (request :post "/")
              (assoc :cookies {"__anti-forgery-token" {:value "foo"}})
              (assoc :form-params {"__anti-forgery-token" "foo"})))))

(deftest token-in-cookie-test
  (let [response {:status 200, :headers {}, :body "Foo"}
        handler  (wrap-anti-forgery (constantly response))]
    (is (contains? (:cookies (handler (request :get "/")))
                   "__anti-forgery-token"))
    (is (not= (get-in (handler (request :get "/"))
                      [:cookies "__anti-forgery-token"])
              (get-in (handler (request :get "/"))
                      [:cookies "__anti-forgery-token"])))))

(deftest token-binding-test
  (letfn [(handler [request]
            {:status 200
             :headers {}
             :body *anti-forgery-token*})]
    (let [response ((wrap-anti-forgery handler) (request :get "/"))]
      (is (= (get-in response [:cookies "__anti-forgery-token"])
             (:body response))))))
