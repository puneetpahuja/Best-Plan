(ns best-plan.utils)

(defn round-two-digits [number]
  (float (/ (Math/round (* (float number) 100)) 100)))
