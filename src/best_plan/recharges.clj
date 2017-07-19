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
(defrecord CostCutterTalktimeCombo [cost-cutter-recharge talktime-recharge cost comments])

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
  (->CostCutterTalktimeCombo cost-cutter-recharge
                             talktime-recharge
                             (+ (:cost cost-cutter-recharge) (:cost talktime-recharge))
                             (str "Cost cutter recharge: "
                                  (:comments cost-cutter-recharge)
                                  "And talktime recharge: "
                                  (:comments talktime-recharge))))

(defn monthly-bill-talktime [{:keys [monthly-talktime cost talktime validity monthly-cost] :as plan}
                             {:keys [talktime-needed] :as user}]
  (assoc plan
         :monthly-bill
         (if validity
           (if (<= talktime-needed monthly-talktime)
             monthly-cost
             (* talktime-needed (/ cost talktime)))
           (* talktime-needed (/ cost talktime)))))

(defn monthly-bill-minutes [{:keys [monthly-minutes cost minutes validity monthly-cost] :as plan}
                            {:keys [total-usage] :as user}]
  (assoc plan
         :monthly-bill
         (if validity
           (if (<= total-usage monthly-minutes)
             monthly-cost
             (* total-usage (/ cost minutes)))
           (* total-usage (/ cost minutes)))))

(defn get-first-value [key [first & rest]]
  (when first
    (if-let [value (key first)]
      value
      (get-first-value key rest))))

(defn monthly-bill-cost-cutter [{{:keys []
                                  cost-cutter-monthly-cost :monthly-cost
                                  :as cost-cutter-plan} :cost-cutter-recharge
                                 {:keys [validity monthly-talktime cost talktime]
                                  talktime-monthly-cost :monthly-cost
                                  :as talktime-plan} :talktime-recharge
                                 :as plan}
                                {:keys [local-usage std-usage] :as user}]
  (let [talktime-needed (+ (* (get-first-value :local-rate [cost-cutter-plan user]) local-usage)
                           (* (get-first-value :std-rate [cost-cutter-plan user]) std-usage))]
    (assoc plan
           :monthly-bill
           (+ cost-cutter-monthly-cost
              (if validity
                (if (<= talktime-needed monthly-talktime)
                  talktime-monthly-cost
                  (* talktime-needed (/ cost talktime)))
                (* talktime-needed (/ cost talktime)))))))


(defprotocol MonthlyBill
  (monthly-bill [plan user] "Gives the monthly bill for the plan given user details"))

(extend-protocol MonthlyBill
  TalktimeRecharge
  (monthly-bill [plan user]
    (monthly-bill-talktime plan user))

  MinutesRecharge
  (monthly-bill [plan user]
    (monthly-bill-minutes plan user))

  CostCutterTalktimeCombo
  (monthly-bill [plan user]
    (monthly-bill-cost-cutter plan user)))
