(ns ring.util.test.anti-forgery
  (:use clojure.test
        ring.util.anti-forgery
        ring.middleware.anti-forgery))

(deftest anti-forgery-field-test
  (binding [*anti-forgery-token* "abc"]
    (is (= (anti-forgery-field)
           (str "<input id=\"__anti-forgery-token\" name=\"__anti-forgery-token\""
                " type=\"hidden\" value=\"abc\" />")))))

(deftest script-token-test
  (binding [*anti-forgery-token* "abc"]
    (is (= (script-token)
           (str "<script type=\"text/javascript\">//<![CDATA[\n"
                "CSRF_TOKEN = \"abc\";\n//]]></script>")))
    (is (= (script-token "otherName")
           (str "<script type=\"text/javascript\">//<![CDATA[\n"
                "otherName = \"abc\";\n//]]></script>")))))
