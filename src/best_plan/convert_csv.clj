(ns best-plan.convert-csv
  (:require [best-plan.env :as env]
            [best-plan.fs :as fs]
            [best-plan.recharge :as recharge]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as comb]
            [clojure.string :as string]))

(defn get-common-details [cost validity]
  (str "Cost: Rs" cost " | Validity: "
       (if validity (str validity "days") "Unlimited")))

(defn maybe
  ([pre-label thing post-label]
   (when thing (str pre-label thing post-label)))
  ([pre-label thing]
   (maybe pre-label thing nil)))

(defn get-cost-cutter-details [cost validity local-rate std-rate]
  (str (get-common-details cost validity)
       (maybe " | Local Call Rate: Rs" local-rate "/minute")
       (maybe " | STD Call Rate: Rs" std-rate "/minute")))

;; (defn get-value [format data field]
;; (get data (.indexOf format field)))

(defn find-category [{:keys [local-rate std-rate]}]
  (cond
    (and local-rate std-rate) :both
    local-rate :local
    :else :std))

(defn categorize-plans [plans]
  (let [categorized-plans {:local #{}
                           :std #{}
                           :both #{}}]
    (reduce (fn [result plan]
              (let [category (find-category plan)]
                (update result category conj plan)))
            categorized-plans
            plans)))

(defn get-days [validity-str]
  (let [[days unit] (string/split validity-str #" ")]
    (if (string/starts-with? unit "day")
      (read-string days)
      "ERROR")))

(defn convert-cost-cutter-row [[_ cost local-rate std-rate validity comments]]
  [cost (recharge/monthly cost validity) local-rate std-rate  validity ;;(get-days validity)
   (get-cost-cutter-details cost validity local-rate std-rate) comments])

(defn process-single-cost-cutter [{:keys [cost monthly-cost local-rate std-rate validity details comments]}]
  [cost monthly-cost local-rate std-rate validity details comments])

(defn process-combo-cost-cutter [[{lcost :cost lmcost :monthly-cost lrate :local-rate
                                   ldetails :details lcomments :comments}
                                  {scost :cost smcost :monthly-cost srate :local-rate
                                   sdetails :details scomments :comments}]]
  [(+ lcost scost) (+ lmcost smcost) lrate srate env/days-in-month
   (str "Local Cost Cutter - " ldetails
        "\nSTD Cost Cutter - " sdetails)
   (str "Local Cost Cutter - " lcomments
        "\nSTD Cost Cutter - " scomments)])

(defn write-csv [filepath rows]
  (io/make-parents filepath)
  (with-open [writer (io/writer filepath)]
    (csv/write-csv writer rows)))

(defn maybe-nil [val]
  (if (nil? val) "nil" val))

(defn stringify-nil [csv-row]
  (map maybe-nil csv-row))

(defn create-cost-cutter-csv [local-csv-filepath std-csv-filepath output-csv-filepath]
  (let [input-csv-rows (concat (fs/get-csv-rows local-csv-filepath)
                               (fs/get-csv-rows std-csv-filepath))
        input-records (map #(apply recharge/cost-cutter-recharge %)
                           (map convert-cost-cutter-row input-csv-rows))
        categorized-plans (categorize-plans input-records)
        local-std-pairs (comb/cartesian-product (:local categorized-plans) (:std categorized-plans))
        output-csv-header-row (map name env/output-cost-cutter-csv-format)
        output-csv-single-plan-rows (map process-single-cost-cutter input-records)
        output-csv-combo-plan-rows (map process-combo-cost-cutter local-std-pairs)
        raw-output-rows (concat [output-csv-header-row] output-csv-single-plan-rows
                                output-csv-combo-plan-rows)
        converted-output-rows (map stringify-nil raw-output-rows)]
    (write-csv output-csv-filepath converted-output-rows)))

(defn process-val [val-str [_ cost val validity comments]]
  (let [validity (if (string/starts-with? (string/lower-case validity) "n.a")
                   nil
                   validity)]
    [cost (recharge/monthly cost validity)
     val (recharge/monthly val validity) validity
     (str (get-common-details cost validity)
          (maybe val-str val)) comments]))

(def process-talktime (partial process-val " | Talktime: Rs"))

(defn create-talktime-csv [top-csv-filepath full-csv-filepath output-csv-filepath]
  (let [input-csv-rows (concat (fs/get-csv-rows top-csv-filepath)
                               (fs/get-csv-rows full-csv-filepath))
        output-csv-header-row (map name env/output-talktime-csv-format)
        output-csv-plan-rows (map process-talktime input-csv-rows)]
    (write-csv output-csv-filepath (map stringify-nil (concat [output-csv-header-row] output-csv-plan-rows)))))

(def process-minutes (partial process-val " | Minutes: "))

(defn create-minutes-csv [minutes-csv-path output-csv-filepath]
  (let [input-csv-rows (fs/get-csv-rows minutes-csv-path)
        output-csv-header-row (map name env/output-minutes-csv-format)
        output-csv-plan-rows (map process-minutes input-csv-rows)]
    (write-csv output-csv-filepath (map stringify-nil (concat [output-csv-header-row] output-csv-plan-rows)))))

(defn apply-with-suffix [func suffix & args]
  (apply func (map #(str suffix % ".csv") args)))

;; (apply-with-suffix create-minutes-csv "resources/plans/KA/airtel/selected/" "raw_minutes" "minutes")
