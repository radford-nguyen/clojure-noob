(ns clojure-noob.algos
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
  (:use [clojure.java.io :only (reader)]))


(defn last-of-sentence? [word]
  (let [last-char (last word)]
    (or (= \. last-char) (= \? last-char))))

(defn words-from [s]
  (re-seq #"\S+" s))

(defn- get-first-words [model]
  "Returns a vector of the first-words of a randomly chosen
  sentence in the given model, or nil if none could be found."
  (loop [m (dissoc model :firsts)]
    (let [start-words (model :firsts)
          words (rand-nth (keys m))
          word (first words)]
      (when (seq m)
        (if (start-words word)
          words
          (recur (dissoc m words)))))))


(defn- get-word [lead, model]
  "Given a coll of words, produces the next word probabilistically
  from the frequency model. If lead does not exist in
  model, then a random word is returned."
  (if-let [next-words (model lead)]
    (rand-nth next-words)
    (rand-nth (rand-nth (keys (dissoc model :firsts))))))


(defn- update-model [model, first-words, next-word]
  (if-let [list-of-next-words (model first-words)]
    (assoc model first-words (conj list-of-next-words next-word))
    (assoc model first-words [next-word])))


(defn- index-start-words [model, prev-words]
  "Returns a new markov model with the :firsts
  updated based on prev-words"
  (let [first-word (last prev-words)]
    (if-let [firsts (model :firsts)]
      (assoc model :firsts (conj firsts first-word))
      (assoc model :firsts #{first-word}))))


(defn- add-word [prev-words, next-word, model]
  (swap! model update-model prev-words next-word))

(defn- process-words
  [words, model
   & {store-first :store-first depth :depth :or {depth 1}}]
  (let [prev-words (subvec words 0 depth)]
    (if store-first
      (swap! model index-start-words prev-words))
    (if (< depth (count words))
      (let [_ (add-word prev-words (nth words depth) model)
            store-first (last-of-sentence? (last prev-words))]
        (recur (subvec words 1)
               model
               {:store-first store-first :depth depth})))))

(defn- add-first-word
  [model word]
  (if-let [firsts (model :firsts)]
    (assoc model :firsts (conj firsts word))
    (assoc model :firsts #{word})))


(defn process-words-
  [words model depth]
  (letfn [(f [segs model]
            (if-let [seg (first segs)]
              (let [lead (drop-last seg)
                    following (last seg)]
                (add-word lead following model)
                (if (last-of-sentence? (last lead))
                  (swap! model add-first-word following))
                (recur (rest segs) model))))]
    (f (partition (inc depth) 1 words) model)))

(defn doo []
  (let [m (atom {})
        w (re-seq #"\S+" "hello world. how are you? i am fine.")]
    (process-words- w m 1)))


(defn process-text
  "Processes the given text and returns a markov model
  representing that text."
  [text & {depth :depth :or {depth 1}}]
  (let [model (atom {})
        data (words-from (str text))]
    (process-words- data model depth)
    @model))

(defn process-file
  "Processes the given text file and returns a markov model
  representing the text."
  [f & {depth :depth :or {depth 1}}]
  (with-open [r (reader f)]
    (process-text (slurp r) :depth depth)))

; (defn gen-freq-data [^String from & {nthreads :nthreads :or {nthreads 1}}]
;   (let [model (atom {})]
;     (dothreads
;       (fn [] (apply process-text [from model]) :threads nthreads))
;     @model))

(defn- next-lead [model lead]
  (let [not-end (comp not last-of-sentence?)]
    (if
      (not-end (first lead))
        (concat (rest lead) [(get-word lead model)]))))

(defn- make-sentence
  "Returns a random sentence, generated from the given markov model."
  [model & {depth :depth :or {depth 1}}]
  (letfn [(sentence [lead]
            (if-let [lead (seq lead)]
              (let [this-w (first lead)
                    lead (next-lead model lead)]
                (cons this-w (lazy-seq (sentence lead))))))]
    (sentence (get-first-words model))))


(defn lazy-sentence [model & {depth :markov-depth :or {depth 1}}]
  (letfn [(f ([] (f (make-sentence model :depth depth)))
             ([s] (cons s (lazy-seq (f (make-sentence model :depth depth))))))]
    (f)))




(defn dooit []
  (let [seed (slurp "seed.txt")
        model (process-text seed :depth 1)
        lead (get-first-words model)]
    (letfn [(sentence [lead]
              (if-let [lead (seq lead)]
                (let [this-w (first lead)
                      lead (next-lead model lead)]
                  (cons this-w (lazy-seq (sentence lead))))))]
      (sentence lead)))) 




