(ns best-plan.save-jsons-datayuge
  (:require [best-plan.env :as env]
            [best-plan.save-jsons-dataweave :as json]
            [best-plan.api-datayuge :as api]
            [clojure.math.combinatorics :as comb]))


(def all-tuples (comb/cartesian-product env/operators env/circles env/recharge-types))

(defn save-jsons
  ([api-key operator circle recharge-type] (save-jsons api-key operator circle recharge-type 1))
  ([api-key operator circle recharge-type page]
   (let [api (api/get-api api-key operator circle recharge-type page)
         json (slurp api)
         data (:data (json/read-json json))]
     (if data
       (do (spit (str env/json-dir operator "_" circle
                      (when recharge-type
                        (str "_" recharge-type))
                      "_" page ".json")
                 json)
                                        ;(Thread/sleep 500)
           (save-jsons api-key operator circle recharge-type (inc page)))
       (when (= page 1) (println api) (spit "failed" (str api "       ") :append true))))))

(defn save-json-helper [vals]
  (apply save-jsons api/api-key vals))

(defn save-all-jsons [n]
  (let [val (nth all-tuples n)]
    ;; (map temp (comb/cartesian-product [(first operators) (second operators)] all-states [nil]))
    (save-json-helper val)
    ;; (Thread/sleep 4000)
    (println n)
    (spit "failed" (str n "\n") :append true)
    (save-all-jsons (inc n))))
