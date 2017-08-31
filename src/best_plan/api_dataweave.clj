(ns best-plan.api-dataweave
  (:require [clojure.tools.html-utils :as html-utils]))

(def api-key "258fed6eab868042008a69905ed9e3e8")
(def api-key-v1 "b20a79e582ee4953ceccf41ac28aa08d")

(defn get-operators-api [api-key page]
  (str "http://api.dataweave.com/v1/telecom_data_v3/listUniqOperator/?api_key="
       api-key
       "&page="
       page
       "&per_page=100"))

(defn get-operator-circles-api [api-key operator page]
  (str "http://api.dataweave.com/v1/telecom_data_v3/listUniqCircle/?api_key="
       api-key
       "&operator="
       (html-utils/url-encode operator)
       "&page="
       page
       "&per_page=100"))

(defn get-operator-recharges-api [api-key operator page]
  (str "http://api.dataweave.com/v1/telecom_data_v3/listByOperator/?api_key="
       api-key
       "&operator="
       (html-utils/url-encode operator)
       "&page="
       page
       "&per_page=100"))

(defn get-operator-circle-recharges-api [api-key operator circle page]
  (str "http://api.dataweave.com/v1/telecom_data_v3/listByCircle/?api_key="
       api-key
       "&operator="
       (html-utils/url-encode operator)
       "&circle="
       (html-utils/url-encode circle)
       "&page="
       page
       "&per_page=100"))
