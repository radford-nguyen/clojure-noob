(ns clojure-noob.core
  "Contains core functions for modeling text using a Markov
  Chain and generating realistic-looking random text using
  that data. The Markov Chain model is represented as a
  map {l f, ...} where:
  
  l         = leading words
  (count l) = Markov depth
  f         = following words
  
  f may contain duplicate words, which indicate that they
  appeared more than once following words l. Contrast
  this approach with storing the frequency information
  explicitly in the model.
  
  The model also contains a set of all the first words
  from any sentence in the text at:
  
  (model :firsts)"
  (:gen-class)
  (:use [clojure.java.io :only (reader)]))


(defn last-of-sentence? [word]
  (let [last-char (last word)]
    (or (= \. last-char) (= \? last-char))))

(defn words-from [s]
  (re-seq #"\S+" s))

(defn- rand-lead [model]
  "Returns a list of the first-words of a randomly chosen
  sentence in the given model, or nil if none could be found."
  (loop [m (dissoc model :firsts)]
    (let [is-start-word? (model :firsts)
          words (rand-nth (keys m))]
      (when (seq m)
        (if (is-start-word? (first words))
          words
          (recur (dissoc m words)))))))

(defn- rand-word [model lead]
  "Given a coll of lead words, produces the next word randomly
  from the frequency model. If lead does not exist in
  model, then a truly random word is returned."
  (if-let [next-words (model lead)]
    (rand-nth next-words)
    (rand-nth (rand-nth (keys (dissoc model :firsts))))))


(defn- update-model [model, lead, following]
  (if-let [list-of-next-words (model lead)]
    (assoc model lead (conj list-of-next-words following))
    (assoc model lead [following])))

(defn- add-word[prev-words, next-word, model]
  (swap! model update-model prev-words next-word))

(defn- add-first-word
  [model word]
  (if-let [firsts (model :firsts)]
    (assoc model :firsts (conj firsts word))
    (assoc model :firsts #{word})))

(defn- process-words
  [model depth words]
  (letfn [(f [segs model]
            (if-let [seg (first segs)]
              (let [lead (drop-last seg)
                    following (last seg)]
                (add-word lead following model)
                (if (last-of-sentence? (last lead))
                  (swap! model add-first-word following))
                (recur (rest segs) model))))]
    (f (partition (inc depth) 1 words) model)))


(defn process-text
  "Processes the given text and returns a markov model
  representing that text."
  [text & {depth :depth :or {depth 1}}]
  (let [model (atom {})
        words (words-from (str text))]
    (process-words model depth words)
    @model))

(defn- next-lead [model lead]
  (let [not-end (comp not last-of-sentence?)]
    (if
      (not-end (first lead))
        (concat (rest lead) [(rand-word model lead)]))))

(defn rand-sentence
  "Returns a random sentence, generated from the given markov model."
  [model & {depth :depth :or {depth 1}}]
  (letfn [(sentence [lead]
            (if-let [lead (seq lead)]
              (let [this-w (first lead)
                    lead (next-lead model lead)]
                (cons this-w (lazy-seq (sentence lead))))))]
    (sentence (rand-lead model))))


(defn rand-sentences [model & {depth :markov-depth :or {depth 1}}]
  "Returns an infinite lazy sequence of random sentences,
  generated from the given markov model."
  (letfn [(f ([] (f (rand-sentence model :depth depth)))
             ([s] (cons s (lazy-seq (f (rand-sentence model :depth depth))))))]
    (f)))



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

