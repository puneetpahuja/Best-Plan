(ns best-plan.core
  (:require [best-plan.recharges :refer :all]
            [best-plan.fs :refer :all]
            [clojure.tools.trace :refer [trace-ns trace-vars untrace-ns untrace-vars] :as t]
            :reload)
  (:gen-class))

(comment TODO
         * combine cost-cutter-recharges for local and std slashes like one only
         local and one only std)

;; call rates are in rupees per minute
;; usage is in minutes

(defn get-best-recharges [user]
  (let [recharges (concat (get-talktime-recharges user)
                          (get-cost-cutter-combos user)
                          (get-minutes-recharges user))]
    (sort-by :monthly-bill (map #(trim-for-user (monthly-bill % user)) recharges))))

;; (def test-user (user "airtel" "KA" 0.6 1.15 200 300))

;; (clojure.pprint/pprint (count (get-best-recharges test-user)))
