# transit-cljs

Transit is a data format and a set of libraries for conveying values between applications written in different languages. This library provides support for marshalling Transit data to/from ClojureScript.

* [Rationale](http://i-should-be-a-link)
* [API docs](http://cognitect.github.io/transit-java/)
* [Specification](http://github.com/cognitect/transit-format)

## Releases and Dependency Information

* Latest release: TBD
* [All Released Versions](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.cognitect%22%20AND%20a%3A%22transit-cljs%22)

[Maven](http://maven.apache.org/) dependency information:

```xml
<dependency>
  <groupId>com.cognitect</groupId>
  <artifactId>transit-cljs</artifactId>
  <version>TBD</version>
</dependency>
```

## Usage

```clojurescript
(ns example
  (:require [com.cognitect.transit-cljs :as t])
  (:import [goog.math Long]))

(defn roundtrip [x]
  (let [w (t/writer :json)
        r (t/reader :json)]
    (t/read r (t/write w x))))

(defn test-roundtrip []
  (let [list1 [:red :green :blue]
        list2 [:apple :pear :grape]
        data  {(Long.fromInt 1) list1
               (Long.fromInt 2) list 2}
        data' (roundtrip data)]
    (asssert (= data data'))))
```

## Development

### Dependencies

Install dependencies with

```
lein deps
```

### Running the tests & benchmarks

Running the tests:

```
lein cljsbuild once test
open index.html
```

In order to run the `bin/verify` tests you must first build the
roundtrip file:

```
lein cljsbuild once roundtrip
```

Running the benchmarks:

```
lein cljsbuild once bench
node target/transit.bench.js
```

## Copyright and License

Copyright © 2014 Cognitect

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
