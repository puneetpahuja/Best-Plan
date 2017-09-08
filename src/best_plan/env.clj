(ns best-plan.env)

(def res "resources/")
(def plans (str res "plans/"))
(def subfolder "selected/")
(def json-dir (str res "raw_jsons_dy/"))

(def drop-cols-final 2) ;; number of columns in the last of final converted csv that are strings
(def drop-cols-raw 1) ;; number of columns in the last of raw unconverted csv that are strings

(def circle-names ["Andhra Pradesh" "Assam" "Bihar Jharkhand" "Chennai" "Delhi NCR"
                   "Gujarat" "Haryana" "Himachal Pradesh" "Jammu Kashmir" "Karnataka"
                   "Kerala" "Kolkata" "Madhya Pradesh Chhattisgarh" "Maharashtra"
                   "Mumbai" "North East" "Odisha" "Punjab" "Rajasthan" "Tamil Nadu"
                   "UP East" "UP West" "West Bengal"])
(def operator-names ["Aircel" "Airtel" "BSNL" "Idea" "Loop Mobile" "MTNL" "MTS"
                     "Reliance CDMA" "Reliance GSM" "T24" "Tata Indicom"
                     "Tata Docomo" "Telenor" "Videocon" "Vodafone" "Jio"])
(def circle-codes {"Andhra Pradesh" "AP", "Assam" "AS", "Bihar Jharkhand" "BI",
                   "Chennai" "CH", "Delhi NCR" "DE", "Gujarat" "GU", "Haryana" "HA",
                   "Himachal Pradesh" "HP", "Jammu Kashmir" "JK", "Karnataka" "KA",
                   "Kerala" "KE", "Kolkata" "KO", "Madhya Pradesh Chhattisgarh" "MP",
                   "Maharashtra" "MA", "Mumbai" "MU", "North East" "NE", "Odisha" "OR",
                   "Punjab" "PU", "Rajasthan" "RA", "Tamil Nadu" "TN", "UP East" "UPE",
                   "UP West" "UPW", "West Bengal" "WBS"})

(def recharge-types ["top" "full" "SMS" "2g" "3g" "4g" "local" "std" "isd"
                     "roaming" "other" "validity" "plan" "frc"])
(def operators ["aircel" "airtel" "bsnl" "idea" "loop_mobile" "mtnl" "mts"
                "reliance_cdma" "reliance_gsm" "t24" "tata_indicom" "tata_docomo"
                "telenor" "videocon" "vodafone" "jio"])
(def circles ["OR" "JK" "HA" "AS" "GU" "NE" "DE" "UPE" "TN" "CH" "KO" "HP" "KA"
              "MA" "AP" "MU" "MP" "PU" "UPW" "KE" "BI" "WBS" "RA"])

(def column-names {:id "id"
                   :operator_id "operator"
                   :circle_id "circle"
                   :recharge_amount "cost"
                   :recharge_talktime "talktime"
                   :recharge_validity "validity"
                   :recharge_short_desc "short description"
                   :recharge_long_desc "comments"
                   :recharge_type "type"})

(def days-in-month 30)


(def input-cost-cutter-csv-format [:id :cost :local-rate :std-rate :validity :comments])
(def output-cost-cutter-csv-format [:cost :monthly-cost :local-rate :std-rate
                                    :validity :details :comments])

(def input-talktime-csv-format [:id :cost :talktime :validity :comments])
(def output-talktime-csv-format [:cost :monthly-cost :talktime :monthly-talktime
                                 :validity :details :comments])

(def input-minutes-csv-format [:id :cost :minutes :validity :comments])
(def output-minutes-csv-format [:cost :monthly-cost :minutes :monthly-minutes
                                :validity :details :comments])
