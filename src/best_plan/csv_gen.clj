(ns best-plan.csv-gen
  (:require [best-plan.save-jsons-dataweave :as save-json]
            [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as comb]
            [clojure.string :as s]))

(def json-dir "resources/raw_jsons_dy/")
(def csv-dir "resources/plans/")
(def states {"Andhra Pradesh" "AP", "Assam" "AS", "Bihar Jharkhand" "BI",
             "Chennai" "CH", "Delhi NCR" "DE", "Gujarat" "GU", "Haryana" "HA",
             "Himachal Pradesh" "HP", "Jammu Kashmir" "JK", "Karnataka" "KA",
             "Kerala" "KE", "Kolkata" "KO", "Madhya Pradesh Chhattisgarh" "MP",
             "Maharashtra" "MA", "Mumbai" "MU", "North East" "NE", "Odisha" "OR",
             "Punjab" "PU", "Rajasthan" "RA", "Tamil Nadu" "TN", "UP East" "UPE",
             "UP West" "UPW", "West Bengal" "WBS"})
(def operators ["Aircel" "Airtel" "BSNL" "Idea" "Loop Mobile" "MTNL" "MTS"
                "Reliance CDMA" "Reliance GSM" "T24" "Tata Indicom"
                "Tata Docomo" "Telenor" "Videocon" "Vodafone" "Jio"])
(def recharge-types ["top" "full" "SMS" "2g" "3g" "4g" "local" "std" "isd"
                     "roaming" "other" "validity" "plan" "frc"])

(def column-names {:id "id"
                   :operator_id "operator"
                   :circle_id "circle"
                   :recharge_amount "cost"
                   :recharge_talktime "talktime"
                   :recharge_validity "validity"
                   :recharge_short_desc "short description"
                   :recharge_long_desc "long description"
                   :recharge_type "type"})

(defn plan->csv-row [plan columns]
  (map #(% plan) columns))

(defn json->csv [json-file columns csv-file is-first?]
  (let [plans (:data (save-json/read-json-file json-file))
        header-row (when is-first? [(plan->csv-row column-names columns)])
        csv-rows (concat header-row
                         (map #(plan->csv-row % columns) plans))]
    (with-open [writer (io/writer csv-file)]
      (csv/write-csv writer csv-rows))))

(defn create-csv
  ([operator circle recharge-type columns number]
   (let [json-file (str json-dir
                        (s/join "_"
                                [operator circle recharge-type number])
                        ".json")]
     (when (.exists (io/as-file json-file))
       (let [csv-file (str csv-dir
                           (s/join "/"
                                   [(states circle) (s/lower-case (s/replace operator #" " "_")) recharge-type])
                           ".csv")]
         (io/make-parents csv-file)
         (json->csv json-file columns csv-file (when (= number 1) true))))))
  ([operator circle recharge-type columns]
   (create-csv operator circle recharge-type columns 1)))

(defn make-csv-files [operators circles recharge-types columns]
  (map #(apply create-csv (concat % [columns]))
       (comb/cartesian-product operators circles recharge-types)))

(make-csv-files operators (keys states) recharge-types [:id :recharge_amount :recharge_talktime :recharge_validity :recharge_short_desc :recharge_long_desc])
