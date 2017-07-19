(ns broadband-scraper.parser
  (:require [hickory.core :as h]))


;;Parse Utils
(defn is-number? [symbol]
  (if (number? symbol)
    symbol
    -1))

(defn coerce-speed [symbol]
  (if (nil? symbol)
    "n/a"
    (-> symbol
        (clojure.string/split #" ")
        second)))

(defn parse-names [names]
  (mapv #(-> %
             (h/parse-fragment)
             first
             (h/as-hickory)
             :content
             first)
        names))

(defn parse-availabilities [availabilities]
  (mapv #(-> %
             (h/parse-fragment)
             first
             (h/as-hickory)
             :content
             first
             (clojure.string/split #"%")
             first
             read-string
             (is-number?))
        availabilities))

(defn parse-tech-types [tech-types]
  (mapv #(-> %
             (h/parse-fragment)
             first
             (h/as-hickory)
             :content
             first)
        tech-types))

(defn parse-prices [prices]
  (mapv #(-> %
             (h/parse-fragment)
             first
             (h/as-hickory)
             :content
             first
             (subs 1)
             (clojure.string/replace #"," "")
             read-string
             (is-number?))
        prices))

(defn parse-speed [speeds]
  (mapv #(-> %
             (h/parse-fragment)
             first
             (h/as-hickory)
             :content
             first
             (coerce-speed))
        speeds))

(defn parse-providers [m]
  (let [provider-map
        {:provider     (parse-names (:provider m))
         :availability (parse-availabilities (:availability m))
         :tech-type    (parse-tech-types (:tech-type m))
         :price        (parse-prices (:price m))
         :plan-speed   (parse-speed (:plan-speed m))
         }]
    provider-map))