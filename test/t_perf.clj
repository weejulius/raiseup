(ns ^{:doc "a set of test suites for performance"}
    t-perf
  (:use [criterium.core]))

(defn fn-with-destructing
  [[a b c & other]]
  (+ a b c))

(defn fn-without-destructing
  [a b c & other]
  (+ a b c))

(defn fn-with-map-destructing
  [{:keys [a b c d e f]}]
  (+ a b c))

(defn fn-with-hint
  [^long a ^long b ^long c]
  (+ a b c))

(defn fn-without-others
  [a b c]
  (+ a b c))

;; "test destructing performance"
;(with-progress-reporting
;  (quick-bench (fn-with-destructing [1 2 3 4 5 6])))
;
;(with-progress-reporting
;  (quick-bench (fn-without-destructing 1 2 3 4 5 6)))
;
;(with-progress-reporting
;  (quick-bench (fn-without-others 1 2 3)))
;
;(with-progress-reporting
;  (quick-bench (fn-with-hint 1 2 3)))
;
;(with-progress-reporting
;   (quick-bench (fn-with-map-destructing {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6})))
