(ns best-plan.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn handler [request-map]
  (response/response (str "<html><body> Your best plans are: "
                          (get-best-recharges (get-user request-map))
                          "</body></html>")))

(defn -main []
  (jetty/run-jetty (-> handler var wrap-reload) {:port 3000 :join? false}))
