(ns clojure-noob.core
  (:gen-class)
  (:use clojure-noob.algos))

(defn- prompt []
  (println "Press <enter> to generate a random sentence, or enter <q> to quit:"))

(defn -main
  "Initializes a text-generator from a seed text file (arg 1) and markov depth (arg 2)"
  [& args]
  (let [seed-f (or (first args) "seed.txt")
        quit? #{"q" "quit"}
        depth (if-let [depth-arg (second args)]
                (Integer/parseInt depth-arg)
                1)
        chain (process-file seed-f :depth depth)
        s (lazy-sentence chain :depth depth)]
    (prompt)
    (loop [quit (quit? (read-line))
           rs s]
      (when-not quit
        (println (first rs))
        (recur (quit? (read-line)) (rest rs))))))

