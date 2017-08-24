(ns best-plan.save-jsons-dataweave
  (:require [best-plan.api-dataweave :as api]
            [clojure.data.json :as json]
            [clojure.string :as s]))

(def dir "resources/raw_jsons/")

(defn read-json [json]
  (json/read-str json :key-fn keyword))

(defn read-json-file [file]
  (read-json (slurp file)))

(defn get-values [json key]
  (map key (:data (read-json json))))

(defn get-operators-json [api-key page]
  (slurp (api/get-operators-api api-key page)))

(defn get-operators [json]
  (get-values json :operator_master))

(defn get-operator-circles-json [api-key operator page]
  (slurp (api/get-operator-circles-api api-key operator page)))

(defn get-operator-circles [json]
  (get-values json :circle_master))

(defn get-operator-recharges-json [api-key operator page]
  (slurp (api/get-operator-recharges-api api-key operator page)))

(defn get-operator-circle-recharges-json [api-key operator circle page]
  (slurp (api/get-operator-circle-recharges-api api-key operator circle page)))

(defn convert-text-to-api [string]
  (s/replace string #" " "%20"))

(defn convert-api-to-text [api-string]
  (s/replace api-string #"%20" "_"))

(defn save-operator-recharges-jsons [operator]
  (let [page-1-json (get-operator-recharges-json api/api-key operator 1)
        count (or (:count (read-json page-1-json)) 0)
        pages (Math/ceil (/ count 10))]
    (println count pages)
    (spit (str dir (convert-api-to-text operator) "_recharges_1.json") page-1-json)
    (map (fn [page]
           (spit (str dir (convert-api-to-text operator) "_recharges_" page ".json")
                 (get-operator-recharges-json api/api-key operator page)))
         (range 2 (inc pages)))))

(defn save-operator-circle-recharges-jsons [operator circle]
  (let [page-1-json (get-operator-circle-recharges-json api/api-key operator circle 1)
        count (or (:count (read-json page-1-json)) 0)
        pages (Math/ceil (/ count 100))]
    (println count pages operator circle)
    (spit (str dir operator "_" circle "_recharges_1.json") page-1-json)
    (map (fn [page]
           (spit (str dir operator "_" circle "_recharges_" page ".json")
                 (get-operator-circle-recharges-json api/api-key operator circle page)))
         (range 2 (inc pages)))))

(defn save-all-jsons []
  (let [operators-json (get-operators-json api/api-key 1)
        operators (get-operators operators-json)]
    (println operators)
    (spit (str dir "operators.json") operators-json)
    (map (fn [operator]
           (let [circles-json (get-operator-circles-json api/api-key operator 1)
                 circles (get-operator-circles circles-json)]
             (println circles)
             (spit (str dir (convert-api-to-text operator) "_circles.json")
                   circles-json)
             (save-operator-recharges-jsons operator)
             (map #(save-operator-circle-recharges-jsons operator %)
                  (map convert-text-to-api circles))))
         (map convert-text-to-api operators))))

(defn save-selected-jsons []
  (let [operators ["Idea" "Vodafone" "Airtel"]]
    (map (fn [operator]
           (let [circles ["Andhra Pradesh" "Karnataka" "Mumbai"]]
             (map #(save-operator-circle-recharges-jsons operator %)
                  circles)))
         operators)))
