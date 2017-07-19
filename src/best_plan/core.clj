(ns best-plan.core
  (:require [best-plan.recharges :refer :all]
            [clojure.math.combinatorics :refer [cartesian-product] :as combo]
            [clojure.tools.trace :as t]
            :reload)
  (:import [best_plan.recharges TalktimeRecharge MinutesRecharge CostCutterTalktimeCombo])
  (:gen-class))

(comment TODO
         * combine cost-cutter-recharges for local and std slashes like one only local and one only std)

;; call rates are in rupees per minute
;; usage is in minutes

(def dummy-talktime-plans (map #(apply talktime-recharge %)
                               [[100 110 nil "Exclusive offer on My Airtel app and Airtel.in"]
                                [50 50 nil "Exclusive offer on My Airtel app and Airtel.in"]
                                [200 220 nil "Exclusive offer on My Airtel app and Airtel.in"]
                                [20 15.39 nil ""]
                                [30 23.09 nil ""]
                                [10 10 10 ""]
                                [5000 8000 20 ""]]))

(def dummy-minutes-plans (map #(apply minutes-recharge %)
                              [[100 1100 nil ""]
                               [20 2000 nil "Exclusive offer on My Airtel app and Airtel.in"]
                               [200 1220 nil "Exclusive offer on My Airtel app and Airtel.in"]
                               [20 145.39 nil ""]
                               [30 24443.09 nil ""]
                               [10 140 10 ""]
                               [500 8000 20 ""]
                               [50 5000 nil ""]]))

(def dummy-cost-cutter-plans (map #(apply cost-cutter-recharge %)
                                  [[14 nil 0.25 28 ""]
                                   [16 0.6 0.6 28 ""]
                                   [17 0.3 nil 28 ""]]))
;; usage is monthly
(defrecord User [telecom-provider circle local-rate std-rate local-usage std-usage total-usage talktime-needed])

(defn user [telecom-provider circle local-rate std-rate local-usage std-usage]
  (->User telecom-provider circle local-rate std-rate local-usage std-usage (+ local-usage std-usage) (+ (* local-rate local-usage) (* std-rate std-usage))))

(defn make-cost-cutter-talktime-pair [[cost-cutter talktime]]
  (cost-cutter-talktime-combo cost-cutter-recharge talktime-recharge))

(defn get-talktime-plans [user]
  dummy-talktime-plans)

(defn get-minutes-plans [user]
  dummy-minutes-plans)

(defn get-cost-cutter-plans [user]
  (map make-cost-cutter-talktime-pair (cartesian-product dummy-cost-cutter-plans dummy-talktime-plans)))

(defn get-best-plans [user]
  (let [plans (concat (get-talktime-plans user)
                      (get-cost-cutter-plans user)
                      (get-minutes-plans user))]
    (sort-by :monthly-bill (map #(monthly-bill % user) plans))))

(def test-user (user 'dummy 'any 0.6 1.15 200 300))

;; (get-best-plans test-user)
