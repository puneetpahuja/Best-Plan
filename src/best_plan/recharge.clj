(ns best-plan.recharge
  (:require [best-plan.utils :as u]
            [clojure.string :as s]))

;; call rates are in rupees per minute
;; usage is in minutes
;; validity is in days

(def days-in-month 30)

(defn monthly [thing validity]
  (when validity (* thing (/ days-in-month validity))))

;; nil means unlimited validity
(defrecord BaseRecharge [cost validity monthly-cost details comments])
(defrecord TalktimeRecharge [talktime monthly-talktime])
(defrecord CostCutterRecharge [local-rate std-rate])
(defrecord MinutesRecharge [minutes monthly-minutes])
(defrecord CostCutterTalktimeCombo [cost-cutter-recharge talktime-recharge cost
                                    details comments])

(defn base-recharge [cost validity comments]
  (let [monthly-cost (monthly cost validity)
        details (str "Cost: Rs" cost " | Validity: "
                     (if validity (str validity "days") "Unlimited"))]
    (->BaseRecharge cost validity monthly-cost details comments)))

(defn talktime-recharge [cost talktime validity comments]
  (let [monthly-talktime (monthly talktime validity)
        base-recharge (base-recharge cost validity comments)]
    (-> (->TalktimeRecharge talktime monthly-talktime)
        (merge base-recharge
               {:details (str (:details base-recharge)
                              " | Talktime: " talktime)}))))

(defn cost-cutter-recharge [cost local-rate std-rate validity comments]
  (let [base-recharge (base-recharge cost validity comments)]
    (-> (->CostCutterRecharge local-rate std-rate)
        (merge  base-recharge
                {:details (str (:details base-recharge)
                               (when local-rate (str " | Local Call Rate: "
                                                     local-rate "Rs/min"))
                               (when std-rate (str " | Std Call Rate: "
                                                   std-rate "Rs/min")))}))))

(defn minutes-recharge [cost minutes validity comments]
  (let [monthly-minutes (monthly minutes validity)
        base-recharge (base-recharge cost validity comments)]
    (->  (->MinutesRecharge minutes monthly-minutes)
         (merge base-recharge
                {:details (str (:details base-recharge)
                               " | Minutes: " minutes)}))))

(defn combine-property [property cost-cutter-recharge talktime-recharge]
  (let [val-c (property cost-cutter-recharge)
        val-t (property talktime-recharge)]
    (cond
      (number? val-c) (+ val-c val-t)
      (string? val-c) (str (when-not (s/blank? val-c)
                             (str "Cost cutter recharge: " val-c ". "))
                           (when-not (s/blank? val-t)
                             (str "Talktime recharge: " val-t ". ")))
      :else :combine-property-error)))

(defn cost-cutter-talktime-combo [cost-cutter-recharge talktime-recharge]
  (->CostCutterTalktimeCombo cost-cutter-recharge
                             talktime-recharge
                             (combine-property :cost cost-cutter-recharge talktime-recharge)
                             (combine-property :details cost-cutter-recharge talktime-recharge)
                             (combine-property :comments cost-cutter-recharge talktime-recharge)))

(defn monthly-bill-talktime [{:keys [monthly-talktime cost talktime validity
                                     monthly-cost] :as recharge}
                             {:keys [talktime-needed] :as user}]
  (assoc recharge
         :monthly-bill
         (u/round-two-digits (if validity
                               (if (<= talktime-needed monthly-talktime)
                                 monthly-cost
                                 (* talktime-needed (/ cost talktime)))
                               (* talktime-needed (/ cost talktime))))))

(defn monthly-bill-minutes [{:keys [monthly-minutes cost minutes validity
                                    monthly-cost] :as recharge}
                            {:keys [total-usage] :as user}]
  (assoc recharge
         :monthly-bill
         (u/round-two-digits (if validity
                               (if (<= total-usage monthly-minutes)
                                 monthly-cost
                                 (* total-usage (/ cost minutes)))
                               (* total-usage (/ cost minutes))))))

(defn get-first-value [key [first & rest]]
  (when first
    (if-let [value (key first)]
      value
      (get-first-value key rest))))

(defn monthly-bill-cost-cutter [{{cost-cutter-monthly-cost :monthly-cost
                                  :as cost-cutter-recharge} :cost-cutter-recharge
                                 {:keys [validity monthly-talktime cost talktime]
                                  talktime-monthly-cost :monthly-cost
                                  :as talktime-recharge} :talktime-recharge
                                 :as recharge}
                                {:keys [local-usage std-usage] :as user}]
  (let [talktime-needed (+ (* (get-first-value :local-rate [cost-cutter-recharge user]) local-usage)
                           (* (get-first-value :std-rate [cost-cutter-recharge user]) std-usage))]
    (assoc recharge
           :monthly-bill
           (u/round-two-digits (+ cost-cutter-monthly-cost
                                  (if validity
                                    (if (<= talktime-needed monthly-talktime)
                                      talktime-monthly-cost
                                      (* talktime-needed (/ cost talktime)))
                                    (* talktime-needed (/ cost talktime))))))))

(defprotocol Recharge
  (monthly-bill [recharge user]
    "Gives the monthly bill for the recharge for the given user details")
  (trim-for-user [recharge]
    "Gives a subset of recharge details that you want to show to the user"))

(def user-attributes [:cost :monthly-bill :details :comments])

(extend-protocol Recharge
  TalktimeRecharge
  (monthly-bill [recharge user]
    (monthly-bill-talktime recharge user))
  (trim-for-user [recharge]
    (select-keys recharge user-attributes))

  CostCutterRecharge
  (monthly-bill [recharge user])
  (trim-for-user [recharge]
    (select-keys recharge user-attributes))

  MinutesRecharge
  (monthly-bill [recharge user]
    (monthly-bill-minutes recharge user))
  (trim-for-user [recharge]
    (select-keys recharge user-attributes))

  CostCutterTalktimeCombo
  (monthly-bill [recharge user]
    (monthly-bill-cost-cutter recharge user))
  (trim-for-user [recharge]
    (select-keys recharge user-attributes)))
