;; Copyright 2014 Cognitect. All Rights Reserved.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;      http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS-IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns transit.test.core
  (:require [transit.core :as t]))

(enable-console-print!)

(def r (t/reader :json))
(def w (t/writer :json))

(println "testing basic transit write")
(assert (= (.write w 1) "{\"~#'\":1}"))
(assert (= (.write w (js/Date. 1399471321791)) "{\"~#'\":\"~m1399471321791\"}"))
(assert (= (.write w {:foo "bar"}) "[\"^ \",\"~:foo\",\"bar\"]"))
(assert (= (.write w [1 2 3]) "[1,2,3]"))
;;(assert (= (.write w #{1 2 3}) "{\"~#set\":[1,2,3]}"))
(assert (= (.write w '(1 2 3)) "{\"~#list\":[1,2,3]}"))
(assert (= (.write w (reverse [1 2 3])) "{\"~#list\":[3,2,1]}"))
(assert (= (.write w (range 3)) "{\"~#list\":[0,1,2]}"))
(assert (= (.write w (take 3 (repeat true))) "{\"~#list\":[true,true,true]}"))
(assert (= (.write w #js [1 2 3]) "[1,2,3]"))
(assert (= (.write w #js {"foo" "bar"}) "[\"^ \",\"foo\",\"bar\"]"))

(println "testing basic transit read")
(assert (= (.read r "{\"~#'\":1}") 1))
(assert (= (.read r "{\"~:foo\":\"bar\"}") {:foo "bar"}))
(assert (= (.read r "[1,2,3]") [1 2 3]))
(assert (= (.read r "{\"~#set\":[1,2,3]}") #{1 2 3}))
(assert (= (.read r "{\"~#list\":[1,2,3]}") '(1 2 3)))
(assert (= (.valueOf (.read r "{\"~#'\":\"~t2014-05-07T14:02:01.791Z\"}"))
           (.valueOf (js/Date. 1399471321791))))

(defn roundtrip [s]
  (.write w (.read r s)))

(println "testing round tripping")
(assert (= (roundtrip "[\"~:foo\",\"~:bar\",[\"^ \",\"^\\\"\",[1,2]]]")
                      "[\"~:foo\",\"~:bar\",[\"^ \",\"^\\\"\",[1,2]]]"))
(assert (= (roundtrip "{\"~#point\":[1,2]}")
                      "{\"~#point\":[1,2]}"))
(assert (= (roundtrip "[\"^ \",\"foo\",\"~xfoo\"]")
                      "[\"^ \",\"foo\",\"~xfoo\"]"))
(assert (= (roundtrip "[\"^ \",\"~/t\",null]")
                      "[\"^ \",\"~/t\",null]"))
(assert (= (roundtrip "[\"^ \",\"~/f\",null]")
                      "[\"^ \",\"~/f\",null]"))
(assert (= (roundtrip "{\"~#'\":\"~f-1.1E-1\"}")
                      "{\"~#'\":\"~f-1.1E-1\"}"))
(assert (= (roundtrip "{\"~#'\":\"~f-1.10E-1\"}")
                      "{\"~#'\":\"~f-1.10E-1\"}"))
(assert (= (roundtrip "{\"~#set\":[{\"~#ratio\":[\"~i4953778853208128465\",\"~i636801457410081246\"]},{\"^\\\"\":[\"~i-8516423834113052903\",\"~i5889347882583416451\"]}]}")
                      "{\"~#set\":[{\"~#ratio\":[\"~i4953778853208128465\",\"~i636801457410081246\"]},{\"^\\\"\":[\"~i-8516423834113052903\",\"~i5889347882583416451\"]}]}"))
(assert (= (roundtrip "[[\"^ \",\"aaaa\",1,\"bbbb\",2],[\"^ \",\"^!\",3,\"^\\\"\",4],[\"^ \",\"^!\",5,\"^\\\"\",6]]")
                      "[[\"^ \",\"aaaa\",1,\"bbbb\",2],[\"^ \",\"^!\",3,\"^\\\"\",4],[\"^ \",\"^!\",5,\"^\\\"\",6]]"))
(assert (= (roundtrip "{\"~#'\":\"~n8987676543234565432178765987645654323456554331234566789\"}")
                      "{\"~#'\":\"~n8987676543234565432178765987645654323456554331234566789\"}"))
(assert (= (roundtrip "{\"~#list\":[0,1,2,true,false,\"five\",\"~:six\",\"~$seven\",\"~~eight\",null]}")
                      "{\"~#list\":[0,1,2,true,false,\"five\",\"~:six\",\"~$seven\",\"~~eight\",null]}"))
;; (assert (= (roundtrip "[\"^ \",\"~:key0000\",0,\"~:key0001\",1,\"~:key0002\",2,\"~:key0003\",3,\"~:key0004\",4,\"~:key0005\",5,\"~:key0006\",6,\"~:key0007\",7,\"~:key0008\",8,\"~:key0009\",9]")
;;                       "[\"^ \",\"~:key0000\",0,\"~:key0001\",1,\"~:key0002\",2,\"~:key0003\",3,\"~:key0004\",4,\"~:key0005\",5,\"~:key0006\",6,\"~:key0007\",7,\"~:key0008\",8,\"~:key0009\",9]"))

(println "ok")
