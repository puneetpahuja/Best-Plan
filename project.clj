(defproject best_plan "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clojure-tools "1.1.3"]
                 [compojure "1.4.0"]
                 [environ "0.5.0"]
                 [hiccup "1.0.5"]
                 [metosin/ring-http-response "0.6.5"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/tools.trace "0.7.9"]
                 [ring "1.4.0"]
                 [selmer "1.0.2"]]
  :resource-paths ["resources"]
  :main ^:skip-aot best-plan.server
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all} :production {:env {:production true}}}
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "best-plan-standalone.jar")
