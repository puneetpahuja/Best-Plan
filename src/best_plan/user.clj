(ns best-plan.user)

;; usage is monthly
(defrecord User [telecom-provider circle local-rate std-rate local-usage
                 std-usage total-usage talktime-needed])

(defn user [telecom-provider circle local-rate std-rate local-usage std-usage]
  (->User telecom-provider circle local-rate std-rate local-usage std-usage
          (+ local-usage std-usage) (+ (* local-rate local-usage)
                                       (* std-rate std-usage))))
