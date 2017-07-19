(ns broadband-scraper.fcc-api
  (:require [taoensso.timbre :as log]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def base-url "http://www.broadbandmap.gov/broadbandmap/")

(def stateids (vec (range 1 57)))

(defn calc-state-id [sid]
     (if (< sid 10) (str "0" sid) (str sid)))

(defn get-state-lookup [state-id]
  (log/info "get-state-lookup")
  (http/get (str base-url "geography/state/"
                 (calc-state-id state-id)
                 "/county?format=json")))

(def state-lookup (get-state-lookup 29))

(defn get-counties [state-id]
  (log/info "get-counties")
  (let [state-json (get-state-lookup state-id)]
    (:Results
      (json/read-str (:body @state-json)
                     :key-fn keyword))))

(def MO-counties (get-counties 29))
(generate-county-string MO-counties)

(defn generate-county-string [m]
  (log/info "generate-county-string")
  (clojure.string/replace
    (clojure.string/trimr
      (reduce str (map #(str (:geographyId %) " ") m)))
    #" " ","))

(defn add-key-value-pair [k v data]
  (log/info "add-key-value-pair")
  (map #(assoc % k v) data))

;; takes list of maps, maps key to column and writes out value
(defn write-csv [path row-data]
  (log/info "write-csv")
  (let [columns (keys (first row-data))
        headers (map name columns)
        rows (mapv #(mapv % columns) row-data)]
    (with-open [file (io/writer path)]
      (csv/write-csv file (cons headers rows)))))

(defn get-demographics [counties]
  (log/info (str "get-demographics: " counties))
  (let [json (http/get (str base-url
                            "demographic/jun2011/county/ids/"
                            counties
                            "?format=json"))]
    (:Results
      (json/read-str (:body @json)
                     :key-fn keyword))))

(defn get-state-demographics [state-id]
  (log/info "get-state-demographics")
  (let [county-list
        (get-counties state-id)
        county-string
        (generate-county-string county-list)
        demographics-map
        (get-demographics county-string)
        demo-map-w-state
        (add-key-value-pair :stateId (calc-state-id state-id) demographics-map)]
    demo-map-w-state))

(def counter (atom 0))

(defn pull-data [states state-mapping]
  (doseq [state states]
    (log/info (str "Pulling state: " ((keyword (str state)) state-mapping)))
    (if (contains? state-mapping (keyword (str state)))
      (do
        (swap! counter inc)
        (let [state-name ((keyword (str state)) state-mapping)
                m (get-state-demographics state)]
          (write-csv (str "../demographics_2011_" @counter ".csv") m)
            (log/info "Written to csv."))))))

;; PULL NATIONAL DATA
;;(pull-data stateids state-mapping)

;; PULL SINGLE STATE DATA
;;(def MO (get-state-demographics 29))
;;(write-csv "../MO_demographics_2011.csv" MO)


;; Collect BTOP Data
;;http://www.broadbandmap.gov/broadbandmap/btop/stateids/01?format=json

(defn get-btop-grant [state]
  (log/info (str "get-btop-grant: " state))
  (let [json (http/get (str base-url
                            "btop/stateids/"
                            state
                            "?format=json"))]
    (:Results
      (json/read-str (:body @json)
                     :key-fn keyword))))



(defn pull-grants [states state-mapping]
  (doseq [state states]
    (log/info (str "Pulling state: " ((keyword (str state)) state-mapping)))
    (if (contains? state-mapping (keyword (str state)))
      (do
        (swap! counter inc)
        (let [state-name ((keyword (str state)) state-mapping)
              m (get-btop-grant state)
              m-filtered (filter #(= (:awardType %) "Infrastructure") m)]
          (write-csv (str "../" state-name "_BTOP_grants_" @counter ".csv") m-filtered)
          (log/info "Written to csv."))))))

;;(pull-grants stateids state-mapping2)

;;(def AL (get-btop-grant (calc-state-id 1)))
;;(def AL-infrastructure (filter #(= (:awardType %) "Infrastructure") AL))
;;(write-csv (str "../" "AL" "_BTOP_grants_" "51" ".csv") AL-infrastructure)

;; Collect State Mapping
;(def state-lookup (http/get "http://www.broadbandmap.gov/broadbandmap/geography/state?format=json&maxresults=1000"))
;(def state-lookup-edn  (:Results (json/read-str (:body @state-lookup) :key-fn keyword)))
;;(write-csv "../state-lookup.csv" state-looksup-edn)

(defn generate-states-string [m]
  (log/info "generate-county-string")
  (clojure.string/replace
    (clojure.string/trim
      (clojure.string/replace
        (reduce str (map str (keys state-mapping)))
        #":" " "))
    #" " ","))

;;(def json (get-btop-grant (generate-states-string state-mapping)))

;;(def BTOP-grants (filter #(= (:awardType %) "Infrastructure") json))

;(write-csv "../BTOP-grants.csv" BTOP-grants)

;;(generate-states-string state-mapping)

(reduce str (map str (keys state-mapping)))

(def state-mapping2 {:1 "AL",
                    :2 "AK",
                    :4 "AZ",
                    :5 "AR",
                    :6 "CA",
                    :8 "CO",
                    :9 "CT"})

(def state-mapping {:1 "AL",
                    :2 "AK",
                    :4 "AZ",
                    :5 "AR",
                    :6 "CA",
                    :8 "CO",
                    :9 "CT",
                    :10 "DE",
                    :11 "DC",
                    :12 "FL",
                    :13 "GA",
                    :15 "HI",
                    :16 "ID",
                    :17 "IL",
                    :18 "IN",
                    :19 "IA",
                    :20 "KS",
                    :21 "KY",
                    :22 "LA",
                    :23 "ME",
                    :24 "MD",
                    :25 "MA",
                    :26 "MI",
                    :27 "MN",
                    :28 "MS",
                    :29 "MO",
                    :30 "MT",
                    :31 "NE",
                    :32 "NV",
                    :33 "NH",
                    :34 "NJ",
                    :35 "NM",
                    :36 "NY",
                    :37 "NC",
                    :38 "ND",
                    :39 "OH",
                    :40 "OK",
                    :41 "OR",
                    :42 "PA",
                    :44 "RI",
                    :45 "SC",
                    :46 "SD",
                    :47 "TN",
                    :48 "TX",
                    :49 "UT",
                    :50 "VT",
                    :51 "VA",
                    :53 "WA",
                    :54 "WV",
                    :55 "WI",
                    :56 "WY"})



(defn csv-reader [fin]
  (with-open [in-file (io/reader fin)]
                   (doall
                     (csv/read-csv in-file))))

;;733
(def fips-file (csv-reader "resources/fips_codes.csv"))
(def dropped-header (drop 1 fips-file))
(def fips-groups (partition 300 dropped-header ))

(defn generate-fips-string [v]
  (log/info "generate-fips-string")
  (if (and (second v) (nth v 2))
    (let [fips-list (map #(str (second %) (nth % 2) " ") v)
          remove-duplicates (distinct fips-list)
          fips-string (reduce str remove-duplicates)
          trimmed (clojure.string/trim fips-string)
          comma-delim (clojure.string/replace trimmed   #" " ",")]
      comma-delim)
    (log/info "index out of order")))

(defn get-demographics [counties]
  (log/info (str "get-demographics: " counties))
  (let [json (http/get (str base-url
                            "demographic/jun2014/county/ids/"
                            counties
                            "?format=json"))]
    (:Results
      (json/read-str (:body @json)
                     :key-fn keyword))))


(def pull-count (atom 0))
(defn pull-demo-data [fips-file]
  (doseq [group fips-file]
         (do
           (swap! pull-count inc)
           (let [county-string (generate-fips-string group)
                 m (get-demographics county-string)]
             (write-csv (str "../demographics_2014_" @pull-count ".csv") m)))))

;;(pull-demo-data fips-groups)

;;http://www.broadbandmap.gov/broadbandmap/cai/jun2014/state/ids/01,02?format=json
(def cai-nat (http/get "http://www.broadbandmap.gov/broadbandmap/cai/jun2011/state/ids/01?format=json"))
(def cai-nat-edn (:Results (json/read-str (:body @cai-nat) :key-fn keyword)))
(write-csv "../cai-al-11.csv" cai-nat-edn)

(defn get-cai [state]
  (let [json
        (http/get
          (str base-url
               "/cai/jun2014/state/ids/"
               state
               "?format=json"))]
    (:Results (json/read-str (:body @json) :key-fn keyword))))

(def testcai (get-cai (calc-state-id 1)))
(calc-state-id 1)
(def test-cai (http/get
       (str base-url
            "/cai/jun2011/state/ids/"
            "01"
            "?format=json")))

(def pull-count (atom 0))
(defn pull-cai [states state-mapping]
  (doseq [state states]
    (log/info (str "Pulling state: " ((keyword (str state)) state-mapping)))
    (if (contains? state-mapping (keyword (str state)))
      (do
        (swap! pull-count inc)
        (let [state-name ((keyword (str state)) state-mapping)
              m (get-cai (calc-state-id state))]
          (write-csv (str "../cai14_" @pull-count ".csv") m)
          (log/info "Written to csv."))))))

(pull-cai stateids state-mapping)

