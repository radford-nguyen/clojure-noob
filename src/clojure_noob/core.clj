(ns clojure-noob.core
  (:gen-class)
  (:use clojure-noob.algos))

(defn- prompt []
  (println "Press <enter> to generate a random sentence, or enter <q> to quit:"))

(defn -main
  "Initializes a text-generator from a seed text file (arg 1) and markov depth (arg 2)"
  [& args]
  (let [seed-file (or (first args) "seed.txt")
        depth (Integer/parseInt (or (second args) "1"))
        quit? #{"q" "quit"}
        model (process-text (slurp seed-file) :depth depth)
        sentences (rand-sentences model :markov-depth depth)]
    (prompt)
    (loop [quit (quit? (read-line))
           [s & rs] sentences]
      (when-not quit
        (println s)
        (recur (quit? (read-line)) rs)))))

