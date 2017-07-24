(ns best-plan.fs
  (:require [clojure.string :as s]
            [best-plan.recharges :refer :all]
            [clojure.java.io :as io]))

;; for future use for big files
;; (defn get-line-seq [{:keys [telecom-provider circle]} filename]
;; (let [reader (-> (str "plans/" circle "/" telecom-provider "/" filename)
;;                  io/resource
;;                  io/reader)]
;;   (line-seq reader)))

(defn file-lines [{:keys [telecom-provider circle]} filename]
  (-> (s/join "/" ["plans" circle telecom-provider filename])
      io/resource
      slurp
      s/split-lines
      rest))

(defn get-recharges [user type init-fn]
  (->> (file-lines user type)
       (map #(read-string (str "[" % "]")))
       (map #(apply init-fn %))))

(defn get-talktime-recharges [user]
  (get-recharges user "talktime" talktime-recharge))

(defn get-minutes-recharges [user]
  (get-recharges user "minutes" minutes-recharge))

(defn get-cost-cutter-recharges [user]
  (get-recharges user "cost_cutter" cost-cutter-recharge))

(defn make-cost-cutter-talktime-pair [[cost-cutter talktime]]
  (cost-cutter-talktime-combo cost-cutter talktime))

(defn get-cost-cutter-combos [user]
  (map make-cost-cutter-talktime-pair
       (get-cost-cutter-recharges user)
       (get-talktime-recharges user)))
