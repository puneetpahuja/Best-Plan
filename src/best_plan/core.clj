(ns best-plan.core
  (:require [best-plan.recharges :refer :all]
            :reload)
  (:gen-class))

;; call rates are in rupees per minute
;; usage is in minutes

;; usage is monthly
(defrecord UserData [local-rate std-rate local-usage std-usage total-usage talktime-needed])

(defn user-data [local-rate std-rate local-usage std-usage]
  (->UserData local-rate std-rate local-usage std-usage (+ local-usage std-usage) (+ (* local-rate local-usage) (* std-rate std-usage))))
