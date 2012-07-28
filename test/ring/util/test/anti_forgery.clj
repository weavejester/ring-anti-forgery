(ns ring.util.test.anti-forgery
  (:use clojure.test
        ring.util.anti-forgery
        ring.middleware.anti-forgery))

(deftest anti-forgery-field-test
  (binding [*anti-forgery-token* "abc"]
    (is (= (anti-forgery-field)
           (str "<input id=\"__anti-forgery-token\" name=\"__anti-forgery-token\""
                " type=\"hidden\" value=\"abc\" />")))))