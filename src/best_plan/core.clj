(ns best-plan.core
  (:require [best-plan.fs :as fs]
            [best-plan.recharge :as r]
            [best-plan.user :as u]
            [clojure.tools.trace :as t]
            :reload)
  (:gen-class))

(comment TODO
         * combine cost-cutter-recharges for local and std slashes like one only
         local and one only std
         * introduce local and std in minutes recharge
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

(def test-user (u/user "airtel" "KA" 0.6 1.15 200 300))

(clojure.pprint/pprint (get-best-recharges test-user))
