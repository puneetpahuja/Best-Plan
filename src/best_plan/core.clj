(ns best-plan.core
  (:require [best-plan.fs :as fs]
            [best-plan.recharge :as r]
            [best-plan.user :as u]
            [clojure.tools.trace :as t]
            :reload))

(comment TODO
         * convert top.csv and full.csv to talktime.csv
         * convert minutes_edit.csv to minutes.csv
         * introduce local and std in minutes recharge
         * add functionality to compare all telecom operators of a circle
         * introduce on-net off-net concept in cost-cutter and minutes
         * add ability to add the concept of first x mins at a costlier rate in
         cost cutter)

;; call rates are in rupees per minute
;; usage is in minutes
(defn get-best-recharges [user]
  (let [recharges (concat (fs/get-talktime-recharges user)
                          (fs/get-cost-cutter-combos user)
                          (fs/get-minutes-recharges user))]
    (sort-by :monthly-bill (map #(r/trim-for-user (r/monthly-bill % user)) recharges))))

;; (def test-user (u/user "airtel" "KA" 0.6 1.15 200 300))
;; (clojure.pprint/pprint (get-best-recharges test-user))
