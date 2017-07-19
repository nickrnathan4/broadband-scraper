(ns broadband-scraper.bbn
  (:require [clj-webdriver.taxi :as taxi]
            [broadband-scraper.parser :refer :all]
            [taoensso.timbre :as log]
            ))

(defn launch-driver [url]
  (taxi/set-driver! {:browser :chrome} url))

(def test-url "http://broadbandnow.com/Alabama/Auburn")

(defn scrape-providers [d]
  (let [provider
        (map #(taxi/html %)
             (taxi/elements d ".provider-detail h3[itemprop='name']"))

        availability
        (map #(taxi/html %)
             (taxi/elements d ".provider-detail .availability .progress-wrap .progress .progress-label"))

        tech-type
        (map #(taxi/html %)
             (taxi/elements d ".provider .overview .tech-type"))

        price
        (map #(taxi/html %)
             (taxi/elements d ".provider .overview .price"))

        speed
        (map #(taxi/html %)
             (taxi/elements d ".provider .overview .plan-speed"))

        ]
    {:provider provider
     :availability availability
     :tech-type tech-type
     :price price
     :plan-speed speed
     }))

(defn write-rows [state city m]
  (loop [x (- (count (:provider m)) 1)]
    (when (or (= x 0) (> x 0))
      (spit (str "../" state ".csv")
            (str
              state ", "
              city ", "
              (nth (:provider m) x) ", "
              (nth (:availability m) x) ", "
              (nth (:tech-type m) x) ", "
              (nth (:price m) x) ", "
              (nth (:plan-speed m) x) "\n"
              )
             :append true
            )
      (recur (- x 1)))))

(defn load-urls [url-file]
  (clojure.string/split
    (slurp url-file)
    #"\n"))

(defn scrape-state [d url-file state]
  (let [city-urls (load-urls url-file)]
    (doseq [url city-urls]
      (log/info (str "Navigating to: " url))
      (taxi/to url)
      (log/info "Waiting for page to load ...")
      (taxi/wait-until #(= (taxi/current-url) url) 15000 1000)
      (log/info (str "Scraping: " url))
           (write-rows
             state
             (last (clojure.string/split url #"/"))
             (parse-providers
               (scrape-providers d)))
           (log/info "Scrape Completed."))
    (log/info (str state " successfully scraped."))))

;;(def d (launch-driver "http://broadbandnow.com"))
;;(scrape-state d "resources/california-urls.txt" "California")
;;(taxi/quit d)