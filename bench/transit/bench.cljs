(ns transit.test
  (:require [transit.core :as t]
            [cljs.reader :as r]))

(enable-console-print!)

(def fs (js/require "fs"))
(def json (. fs (readFileSync "../transit/seattle-data0.tjs" "utf8")))
(def r (t/reader :json))

(println "100 iters, transit read seattle file")
(println (. r (read json)))
(time
  (dotimes [_ 100]
    (. r (read json))))

(println "100 iters, cljs.reader/read-string seattle file")
(def edn (pr-str (. r (read json))))
(time
  (dotimes [_ 100]
    (r/read-string edn)))
