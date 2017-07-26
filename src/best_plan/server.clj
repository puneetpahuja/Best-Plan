(ns best-plan.server
  (:require [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]
            [ring.middleware.params :refer [wrap-params]]
            [best-plan.core :refer [get-best-recharges]]
            [best-plan.recharges :refer [user]]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :as walk]))

(defn show-input-form []
  {:body (slurp (io/resource "html/input.html"))
   :status 200})

(defn show-plans [{:strs [telecom-provider circle local-rate std-rate local-usage
                          std-usage]}]
  {:body (-> (get-best-recharges (user telecom-provider circle
                                       (read-string local-rate)
                                       (read-string std-rate)
                                       (read-string local-usage)
                                       (read-string std-usage)))
             pprint
             with-out-str)
   :status 200})

(defn handler [req]
  (let [user (:form-params req)]
    (if (and user (not-empty user))
      (show-plans user)
      (show-input-form))))

(defn -main []
  (jetty/run-jetty (wrap-params handler) {:port 3000}))
