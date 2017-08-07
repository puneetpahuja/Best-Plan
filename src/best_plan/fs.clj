(ns best-plan.fs
  (:require [best-plan.recharge :as r]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as comb]
            [clojure.string :as s]))

(defn file-lines [{:keys [telecom-provider circle]} recharge-type]
  (with-open [file (-> (s/join "/" ["plans" circle telecom-provider recharge-type])
                       (str ".csv")
                       io/resource
                       io/reader)]
    (doall (rest (csv/read-csv file)))))

(defn process-csv [csv-row]
  (conj (vec (map read-string (butlast csv-row))) (last csv-row)))

(defn get-recharges [user type init-fn]
  (->> (file-lines user type)
       (map #(apply init-fn (process-csv %)))))

(defn get-talktime-recharges [user]
  (get-recharges user "talktime" r/talktime-recharge))

(defn get-minutes-recharges [user]
  (get-recharges user "minutes" r/minutes-recharge))

(defn get-cost-cutter-recharges [user]
  (get-recharges user "cost_cutter" r/cost-cutter-recharge))

(defn make-cost-cutter-talktime-pair [[cost-cutter talktime]]
  (r/cost-cutter-talktime-combo cost-cutter talktime))

(defn get-cost-cutter-combos [user]
  (map make-cost-cutter-talktime-pair
       (comb/cartesian-product (get-cost-cutter-recharges user)
                               (get-talktime-recharges user))))
