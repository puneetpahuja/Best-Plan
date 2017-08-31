(ns best-plan.fs
  (:require [best-plan.env :as env]
            [best-plan.recharge :as r]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as comb]
            [clojure.string :as s]))

(defn read-csv [file]
  (with-open [reader (io/reader file)]
    (doall (rest (csv/read-csv reader)))))

(defn file-lines [{:keys [telecom-provider circle]} recharge-type]
  (read-csv (str env/plans (s/join "/" [circle telecom-provider recharge-type])
                 ".csv")))

(defn process-csv-row [csv-row]
  (concat (map read-string (drop-last 2 csv-row)) (take-last 2 csv-row)))

(defn get-csv-rows [file]
  (map process-csv-row (read-csv file)))

(defn get-recharges [user type init-fn]
  (->> (file-lines user type)
       (map #(apply init-fn (process-csv-row %)))))

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
