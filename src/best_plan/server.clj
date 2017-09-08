(ns best-plan.server
  (:require [best-plan.core :as core]
            [best-plan.user :as user]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [environ.core :as environ]
            [hiccup.core :as hiccup]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [ring.util.response :as resp])
  (:gen-class))

(defn show-input-form []
  {:body (slurp (io/resource "public/input_materialize.html"))
   :status 200})

(defn get-recharges [{:strs [telecom-provider circle local-rate std-rate
                             local-usage std-usage]}]
  (core/get-best-recharges (user/user telecom-provider circle (read-string local-rate)
                                      (read-string std-rate) (read-string local-usage)
                                      (read-string std-usage))))

(defn get-table-data [data]
  [:td data])

(defn get-table-row [& cells]
  (into [:tr] (map get-table-data cells)))

(defn get-table-heading-data [data]
  [:th ;; {:scope "col"}
   data])

(defn get-table-heading-row [& cells]
  (into [:thead] [(into [:tr] (map get-table-heading-data cells))]))

(defn get-table-row-user [{:keys [cost details comments monthly-bill]}]
  (get-table-row details comments cost monthly-bill))

(defn get-output-html [user]
  (hiccup/html [:html
                [:head
                 [:title "Best Plans"]
                 [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons"
                         :rel "stylesheet"}]
                 [:link {:href "https://fonts.googleapis.com/css?family=Roboto"
                         :rel "stylesheet"}]
                 [:link {:href "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.100.2/css/materialize.min.css"
                         :rel "stylesheet"
                         :type "text/css"
                         :media "screen,projection"}]
                 [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                 ;; [:style "table {border-collapse: collapse; border-spacing: 0px;}
                 ;;   table, th, td {padding: 5px; border: 1px solid black;}
                 ;;   td {text-align:right;}
                 ;;   html {font-family: \"Roboto\", sans-serif;}"]
                 ]
                [:body
                 (vec
                   (concat [:table {:class "bordered striped"
                                    :border "1" :cellpadding "10"
                                    :border-collapse "collapse"}
                            [:col {:width "47%"}]
                            [:col {:width "37%"}]
                            [:col {:width "6%" :align "right"}]
                            [:col {:width "10%" :align "right"}]
                            (get-table-heading-row "Details" "Comments" "Cost"
                                                   "Monthly Bill")
                            (into [:tbody]
                                  (map get-table-row-user (get-recharges user)))]))
                 [:script {:type "text/javascript"
                           :src "https://code.jquery.com/jquery-3.2.1.min.js"}]
                 [:script {:type "text/javascript"
                           :src "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.100.2/js/materialize.min.js"}]
                 [:script "$(document).ready(function() {$('select').material_select();});"]]]))

(defn show-plans [user]
  {:body (get-output-html user)
   :status 200})

(defn handler [req]
  (let [user (:form-params req)]
    (if (and user (not-empty user))
      (show-plans user)
      (resp/resource-response "input_materialize.html" {:root "public"}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (environ/env :port) 3000))]
    (jetty/run-jetty (params/wrap-params handler) {:port port :join? false})))
