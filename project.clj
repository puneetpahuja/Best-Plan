(defproject best_plan "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/tools.trace "0.7.9"]]
  :resource-paths ["resources"]
  :main ^:skip-aot best-plan.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
