(ns best-plan.api-datayuge
  (:require [clojure.tools.html-utils :as html-utils]))

(def api-key "dummy")

(def states ["Andhra Pradesh" "Chennai" "Delhi NCR" "Karnataka" "Kerala"
             "Kolkata" "Maharashtra" "Mumbai" "Tamil Nadu" "West Bengal"])
(def circles ["Vodafone" "Jio"])
(def recharge-types ["full" "local" "std"])

(defn get-api [api-key operator circle recharge-type page]
  (str  "http://api.datayuge.in/v6/rechargeplans/?apikey="
        api-key
        "&operator_id=" (html-utils/url-encode operator)
        "&circle_id=" (html-utils/url-encode circle)
        (when recharge-type (str "&recharge_type=" recharge-type))
        "&page=" page
        "&limit=50"))
