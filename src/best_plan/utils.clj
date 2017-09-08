(ns best-plan.utils
  (:require [best-plan.env :as env]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.math.combinatorics :as comb]
            [clojure.string :as string]))

(defn round-two-digits [number]
  (float (/ (Math/round (* (float number) 100)) 100)))

(defn exists? [path]
  (.exists (io/as-file path)))

(defn write-csv [filepath rows]
  (io/make-parents filepath)
  (with-open [writer (io/writer filepath)]
    (csv/write-csv writer rows)))

(defn copy-file [source-path dest-path]
  (when (exists? source-path)
    (io/make-parents dest-path)
    (io/copy (io/file source-path) (io/file dest-path))))

(def all-paths (map #(str env/plans (string/join "/" %) "/")
                    (comb/cartesian-product env/circles env/operators)))

(defn copy-file-in-folder [parent-folder-path dest-subfolder-path
                           filename]
  (copy-file (str parent-folder-path filename)
             (str parent-folder-path dest-subfolder-path filename)))

(defn edit-and-copy-cost-cutter [parent-folder-path dest-subfolder-path
                                 local-cost-cutter-filename std-cost-cutter-filename]
  (copy-file-in-folder parent-folder-path dest-subfolder-path local-cost-cutter-filename)
  (copy-file-in-folder parent-folder-path dest-subfolder-path std-cost-cutter-filename)
  (write-csv (str parent-folder-path dest-subfolder-path "minutes_input.csv")
             [(map name env/input-minutes-csv-format)])
  (write-csv (str parent-folder-path dest-subfolder-path
                  "unprocessed.csv")
             [(map name env/input-talktime-csv-format)]))

(defn copy-files [folder-path]
  (when (exists? folder-path)
    (edit-and-copy-cost-cutter folder-path env/subfolder "local.csv" "std.csv")
    (map #(copy-file-in-folder folder-path env/subfolder %) ["full.csv"
                                                             "top.csv"])))

;; (map copy-files all-paths)
