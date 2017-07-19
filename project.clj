(defproject broadband-scraper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-webdriver/clj-webdriver "0.6.0"]
                 [hickory "0.5.4"]
                 [org.jsoup/jsoup "1.8.1"]
                 [com.taoensso/timbre "3.4.0"]
                 [http-kit "2.1.16"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot broadband-scraper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
