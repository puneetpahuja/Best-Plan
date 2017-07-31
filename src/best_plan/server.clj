(ns best-plan.server
  (:require [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]
            [ring.middleware.params :refer [wrap-params]]
            [best-plan.core :refer [get-best-recharges]]
            [best-plan.recharges :refer [user]]
            [clojure.pprint :refer [pprint]]
            [hiccup.core :refer [html]]
            [ring.util.response :as resp]))

(defn show-input-form []
  {:body (slurp (io/resource "public/input_vanilla.html"))
   :status 200})

(defn get-recharges [{:strs [telecom-provider circle local-rate std-rate
                             local-usage std-usage]}]
  (get-best-recharges (user telecom-provider circle (read-string local-rate)
                            (read-string std-rate) (read-string local-usage)
                            (read-string std-usage))))

(defn get-table-data [data]
  [:td data])

(defn get-table-row [& cells]
  (into [:tr] (map get-table-data cells)))

(defn get-table-heading-data [data]
  [:th {:scope "col"} data])

(defn get-table-heading-row [& cells]
  (into [:tr] (map get-table-heading-data cells)))

(defn get-table-row-user [{:keys [cost details comments monthly-bill]}]
  (get-table-row details comments cost monthly-bill))

(defn get-output-html [user]
  (html [:html
         [:head
          [:title "Best Plans"]
          [:link {:href "https://fonts.googleapis.com/css?family=Roboto"
                  :rel "stylesheet"}]
          [:style "table {border-collapse: collapse; border-spacing: 0px;}
                   table, th, td {padding: 5px; border: 1px solid black;}
                   td {text-align:right;}
                   html {font-family: \"Segoe UI\", \"Roboto\", sans-serif;}"]]
         [:body (vec (concat [:table {:border "1" :cellpadding "10"
                                      :border-collapse "collapse"}
                              [:col {:width "47%"}]
                              [:col {:width "37%"}]
                              [:col {:width "6%" :align "right"}]
                              [:col {:width "10%" :align "right"}]
                              (get-table-heading-row "Details" "Comments" "Cost"
                                                     "Monthly Bill")]
                             (map get-table-row-user (get-recharges user))))]]))
(defn show-plans [user]
  {:body (get-output-html user)
   :status 200})

(defn handler [req]
  (let [user (:form-params req)]
    (if (and user (not-empty user))
      (show-plans user)
      (resp/resource-response "input_vanilla.html" {:root "public"})
      ;;(show-input-form)
      )))

(defn -main []
  (jetty/run-jetty (wrap-params handler) {:port 3000}))
