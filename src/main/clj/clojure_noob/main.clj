(ns clojure-noob.main
  (:use clojure-noob.algos)
  (:use clojure-noob.seeds))
  

(defn sentence-generator [seed-data & {depth :markov-depth :or {depth 1}}]
  (let [corpus (atom {})]

    (process-text seed-data corpus :depth depth)
    (fn [& {op :op :or {op :make-sentence}}]
      (let [op-map
            {:show-first-words #(@corpus :starter-index)
             :make-sentence #(make-sentence @corpus :depth depth)
             :show-corpus @corpus}]
        (op-map op)))))


(def f1 (sentence-generator blurb-seed :markov-depth 1))
(def f3 (sentence-generator blurb-seed :markov-depth 3))


