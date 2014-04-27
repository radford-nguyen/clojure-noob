(ns clojure-noob.core
  (:gen-class)
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

(defn- prompt []
  (println "Press <enter> to generate a random sentence, or enter <q> to quit:"))

(defn -main
  "Initializes a text-generator from a seed text file (arg 1) and markov depth (arg 2)"
  [& args]
  (let [seed-f (or (first args) "seed.txt")
        seed (slurp seed-f)
        quit? #{"q" "quit"}
        depth (if-let [depth-arg (second args)]
                (Integer/parseInt depth-arg)
                3)
        gen (sentence-generator seed :markov-depth depth)]
    (prompt)
    (loop [quit (quit? (read-line))]
      (when-not quit
        (println ((gen)))
        (recur (quit? (read-line)))))))
