(ns best-plan.gen-csv
  (:require [best-plan.env :as env]
            [best-plan.save-jsons-dataweave :as save-json]
            [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as comb]
            [clojure.string :as s]))

(defn plan->csv-row [plan columns]
  (map #(% plan) columns))

(defn json->csv [json-file columns csv-file is-first?]
  (let [plans (:data (save-json/read-json-file json-file))
        header-row (when is-first? [(plan->csv-row env/column-names columns)])
        csv-rows (concat header-row
                         (map #(plan->csv-row % columns) plans))]
    (with-open [writer (io/writer csv-file)]
      (csv/write-csv writer csv-rows))))

(defn create-csv
  ([operator circle recharge-type columns number]
   (let [json-file (str env/json-dir
                        (s/join "_"
                                [operator circle recharge-type number])
                        ".json")]
     (when (.exists (io/as-file json-file))
       (let [csv-file (str env/plans
                           (s/join "/"
                                   [(env/circle-codes circle) (s/lower-case (s/replace operator #" " "_")) recharge-type])
                           ".csv")]
         (io/make-parents csv-file)
         (json->csv json-file columns csv-file (when (= number 1) true))))))
  ([operator circle recharge-type columns]
   (create-csv operator circle recharge-type columns 1)))

(defn make-csv-files [operators circles recharge-types columns]
  (map #(apply create-csv (concat % [columns]))
       (comb/cartesian-product operators circles recharge-types)))

;; (make-csv-files env/operators (keys env/circle-codes) env/recharge-types [:id :recharge_amount :recharge_talktime :recharge_validity :recharge_long_desc])
