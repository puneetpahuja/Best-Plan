(ns best-plan.recharges)

;; call rates are in rupees per minute
;; usage is in minutes
;; validity is in days

(def days-in-month 30)

(defn monthly [thing validity]
  (when validity (/ (* thing days-in-month) validity)))

;; nil means unlimited validity
(defrecord Recharge [cost validity monthly-cost details internal-details comments user-info])
(defrecord TalktimeRecharge [talktime monthly-talktime])
(defrecord CostCutterRecharge [local-rate std-rate])
(defrecord MinutesRecharge [minutes monthly-minutes])
(defrecord CostCutterTalktimeCombo [cost-cutter-recharge talktime-recharge cost monthly-cost comments])

(defn recharge [cost validity comments]
  (let [monthly-cost (monthly cost validity)
        details (str "Cost: Rs" cost " Validity: "
                     (if validity (str validity "days") "Unlimited"))
        internal-details (str "Monthly cost: " monthly-cost)]
    (->Recharge cost validity monthly-cost details internal-details comments nil)))

(defn talktime-recharge [cost talktime validity comments]
  (let [monthly-talktime (monthly talktime validity)
        base-recharge (recharge cost validity comments)]
    (-> (->TalktimeRecharge talktime monthly-talktime)
        (merge base-recharge
               {:details (str (:details base-recharge) " Talktime: " talktime)}))))

(defn cost-cutter-recharge [cost local-rate std-rate validity comments]
  (let [base-recharge (recharge cost validity comments)]
    (-> (->CostCutterRecharge local-rate std-rate)
        (merge  base-recharge
                {:details (str (:details base-recharge)
                               (when local-rate (str " Local Call Rate: " local-rate "Rs/min"))
                               (when std-rate (str " Std Call Rate: " std-rate "Rs/min")))}))))

(defn minutes-recharge [cost minutes validity comments]
  (let [monthly-minutes (monthly minutes validity)
        base-recharge (recharge cost validity comments)]
    (->  (->MinutesRecharge minutes monthly-minutes)
         (merge base-recharge
                {:details (str (:details base-recharge)
                               " Minutes: " minutes)
                 :internal-details (str (:internal-details base-recharge)
                                        " Monthly minutes: " monthly-minutes)}))))

(defn cost-cutter-talktime-combo [cost-cutter-recharge talktime-recharge]
  (->CostCutterTalktimeCombo cost-cutter-recharge talktime-recharge (+ (:cost cost-cutter-recharge)) ()))
