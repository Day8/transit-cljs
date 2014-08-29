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
  (:require [cognitect.transit :as t]))

(enable-console-print!)

(def r (t/reader :json))
(def w (t/writer :json))

(println "testing basic transit write")
(assert (= (t/write w 1) "[\"~#'\",1]"))
(assert (= (t/write w (js/Date. 1399471321791)) "[\"~#'\",\"~m1399471321791\"]"))
(assert (= (t/write w {:foo "bar"}) "[\"^ \",\"~:foo\",\"bar\"]"))
(assert (= (t/write w [1 2 3]) "[1,2,3]"))
;;(assert (= (t/write w #{1 2 3}) "{\"~#set\":[1,2,3]}"))
(assert (= (t/write w '(1 2 3)) "[\"~#list\",[1,2,3]]"))
(assert (= (t/write w (reverse [1 2 3])) "[\"~#list\",[3,2,1]]"))
(assert (= (t/write w (range 3)) "[\"~#list\",[0,1,2]]"))
(assert (= (t/write w (take 3 (repeat true))) "[\"~#list\",[true,true,true]]"))
(assert (= (t/write w #js [1 2 3]) "[1,2,3]"))
(assert (= (t/write w #js {"foo" "bar"}) "[\"^ \",\"foo\",\"bar\"]"))

(println "testing basic transit read")
(assert (= (t/read r "{\"~#'\":1}") 1))
(assert (= (t/read r "{\"~:foo\":\"bar\"}") {:foo "bar"}))
(assert (= (t/read r "[1,2,3]") [1 2 3]))
(assert (= (t/read r "[\"~#set\",[1,2,3]]") #{1 2 3}))
(assert (= (t/read r "[\"~#list\",[1,2,3]]") '(1 2 3)))
(assert (= (.valueOf (t/read r "{\"~#'\":\"~t2014-05-07T14:02:01.791Z\"}"))
           (.valueOf (js/Date. 1399471321791))))

(defn roundtrip [s]
  (t/write w (t/read r s)))

(println "testing round tripping")
(assert (= (roundtrip "[\"~:foo\",\"~:bar\",[\"^ \",\"^1\",[1,2]]]")
                      "[\"~:foo\",\"~:bar\",[\"^ \",\"^1\",[1,2]]]"))
(assert (= (roundtrip "[\"~#point\",[1,2]]")
                      "[\"~#point\",[1,2]]"))
(assert (= (roundtrip "[\"^ \",\"foo\",\"~xfoo\"]")
                      "[\"^ \",\"foo\",\"~xfoo\"]"))
(assert (= (roundtrip "[\"^ \",\"~/t\",null]")
                      "[\"^ \",\"~/t\",null]"))
(assert (= (roundtrip "[\"^ \",\"~/f\",null]")
                      "[\"^ \",\"~/f\",null]"))
(assert (= (roundtrip "{\"~#'\":\"~f-1.1E-1\"}")
                      "[\"~#'\",\"~f-1.1E-1\"]"))
(assert (= (roundtrip "{\"~#'\":\"~f-1.10E-1\"}")
                      "[\"~#'\",\"~f-1.10E-1\"]"))
(assert (= (roundtrip "[\"~#set\",[[\"~#ratio\",[\"~i4953778853208128465\",\"~i636801457410081246\"]],[\"^1\",[\"~i-8516423834113052903\",\"~i5889347882583416451\"]]]]")
                      "[\"~#set\",[[\"~#ratio\",[\"~i4953778853208128465\",\"~i636801457410081246\"]],[\"^1\",[\"~i-8516423834113052903\",\"~i5889347882583416451\"]]]]"))
(assert (= (roundtrip "[[\"^ \",\"aaaa\",1,\"bbbb\",2],[\"^ \",\"^0\",3,\"^1\",4],[\"^ \",\"^0\",5,\"^1\",6]]")
                      "[[\"^ \",\"aaaa\",1,\"bbbb\",2],[\"^ \",\"^0\",3,\"^1\",4],[\"^ \",\"^0\",5,\"^1\",6]]"))
(assert (= (roundtrip "{\"~#'\":\"~n8987676543234565432178765987645654323456554331234566789\"}")
                      "[\"~#'\",\"~n8987676543234565432178765987645654323456554331234566789\"]"))
(assert (= (roundtrip "[\"~#list\",[0,1,2,true,false,\"five\",\"~:six\",\"~$seven\",\"~~eight\",null]]")
                      "[\"~#list\",[0,1,2,true,false,\"five\",\"~:six\",\"~$seven\",\"~~eight\",null]]"))
;; (assert (= (roundtrip "[\"^ \",\"~:key0000\",0,\"~:key0001\",1,\"~:key0002\",2,\"~:key0003\",3,\"~:key0004\",4,\"~:key0005\",5,\"~:key0006\",6,\"~:key0007\",7,\"~:key0008\",8,\"~:key0009\",9]")
;;                       "[\"^ \",\"~:key0000\",0,\"~:key0001\",1,\"~:key0002\",2,\"~:key0003\",3,\"~:key0004\",4,\"~:key0005\",5,\"~:key0006\",6,\"~:key0007\",7,\"~:key0008\",8,\"~:key0009\",9]"))

;; cmap
(def cmap
  (->> {[] 42}
    (t/write (t/writer :json))
    (t/read (t/reader :json))))

(assert (satisfies? cljs.core/IMap cmap))
(assert (= cmap {[] 42}))

(println "----------")
(println "constructor & predicates API")

(def p0 (t/read r "{\"~#point\":[1.5,2.5]}"))
(assert (t/tagged-value? p0))
(def p1 (t/read r "{\"~#point\":[1.5,2.5]}"))
(assert (= p0 p1))
(def m0 {p0 :foo})
(assert (= (get m0 p0) :foo))
(def uuid0 (t/read r "{\"~#'\":\"~u2f9e540c-0591-eff5-4e77-267b2cb3951f\"}"))
(assert (t/uuid? uuid0))
(def uuid1 (t/read r "{\"~#'\":\"~u2f9e540c-0591-eff5-4e77-267b2cb3951f\"}"))
(assert (= uuid0 uuid1))
(def m1 {uuid0 :bar})
(assert (= (get m1 uuid0) :bar))
(def l0 (t/read r "{\"~#'\":\"~i9007199254740993\"}"))
(assert (t/integer? l0))
(def l1 (t/read r "{\"~#'\":\"~i9007199254740993\"}"))
(def m2 {l0 :baz})
(assert (= (get m2 l0) :baz))

;; TCLJS-3
(assert (= (t/read (t/reader :json {:handlers {"custom" (fn [x] x)}}) "[\"~:foo\", 1]")
           [:foo 1]))

(defrecord Point [x y])

(deftype PointHandler []
  Object
  (tag [_ v] "point")
  (rep [_ v] #js [(.-x v) (.-y v)])
  (stringRep [_ v] nil))

(def cr (t/reader :json
          {:handlers
           {"custom" (fn [x] x)
            "point" (fn [[x y]] (Point. x y))}}))

(assert (= (t/read cr "[\"~#point\",[1.5,2.5]]")
           (Point. 1.5 2.5)))

(assert (= (t/read cr "[\"~:foo\", 1]")
           [:foo 1]))

(def cw (t/writer :json
          {:handlers
           {Point (PointHandler.)}}))

(assert (= (t/write cw (Point. 1.5 2.5))
           "[\"~#point\",[1.5,2.5]]"))

;; CLJS UUID

(assert (= (t/read r "{\"~#'\":\"~u550e8400-e29b-41d4-a716-446655440000\"}")
           #uuid "550e8400-e29b-41d4-a716-446655440000"))
(assert (= #uuid "550e8400-e29b-41d4-a716-446655440000"
           (t/read r "{\"~#'\":\"~u550e8400-e29b-41d4-a716-446655440000\"}")))
(assert (= (t/write w #uuid "550e8400-e29b-41d4-a716-446655440000")
           "[\"~#'\",\"~u550e8400-e29b-41d4-a716-446655440000\"]"))

(println "ok")
