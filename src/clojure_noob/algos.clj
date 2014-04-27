(ns clojure-noob.algos
  "Contains core functions for modeling text using a Markov
  Chain and generating realistic-looking random text using
  that index data. The Markov Chain is represented as a
  map {f s} where:
  
  f         = vector of words
  (count f) = Markov depth
  s         = vector of following words
  
  The vector s may contain duplicate words, which indicate
  that they appeared more than once following words f. Contrast
  this approach with storing the frequency information explicitly
  in a map.")


(defn last-of-sentence? [word]
  (let [last-char (last word)]
    (or (= \. last-char) (= \? last-char))))

(defn words-from [s]
  (clojure.string/split s #"\s+"))


(defn get-first-words [freq-hash & {acc :acc :or {acc 0}}]
  "Returns a vector of first-words from a sentence, chosen
  at random from the freq-hash corpus data."
  (let [words (rand-nth (keys (dissoc freq-hash :starter-index)))
        word (first words)
        corpus-size (count freq-hash)]
    (if (contains? (freq-hash :starter-index) word)
      words
      (if (>= acc (* corpus-size corpus-size))
        ; avoid infinite looping
        ["ERROR: You have seeded the generator with data that does not exist in the corpus."]
        (recur freq-hash {:acc (inc acc)})))))


(defn get-word [words, freq-hash]
  "Given a vector of words, produces the next word probabilistically
  from the frequency hash. If words does not exist in
  freq-hash, then a random word is returned."
  (if-let [next-words (freq-hash words)]
    (rand-nth next-words)
    (rand-nth (rand-nth (keys (dissoc freq-hash :starter-index))))))


(defn update-freq-hash [freq-hash, first-words, next-word]
  (if-let [list-of-next-words (freq-hash first-words)]
    (assoc freq-hash first-words (conj list-of-next-words next-word))
    (assoc freq-hash first-words [next-word])))


(defn index-start-words [freq-hash, prev-words]
  "Returns a new corpus map with the starter-index
  updated based on prev-words"
  (if-let [starter-words (freq-hash :starter-index)]
    (assoc freq-hash :starter-index (conj starter-words (last prev-words)))
    (assoc freq-hash :starter-index #{(last prev-words)})))









(defn add-word [prev-words, next-word, ^clojure.lang.Atom freq-hash]
  (swap! freq-hash update-freq-hash prev-words next-word))


(defn process-word-list
  
  [word-list, ^clojure.lang.Atom freq-hash
   & {store-first :store-first depth :depth :or {depth 1}}]

  (let [prev-words (subvec word-list 0 depth)]
    (if store-first
      (swap! freq-hash index-start-words prev-words))
    (if (< depth (count word-list))
      (let [_ (add-word prev-words (nth word-list depth) freq-hash)
            store-first (last-of-sentence? (last prev-words))]
        (recur (subvec word-list 1)
               freq-hash
               {:store-first store-first :depth depth})))))


(defn process-text
  [^String text, ^clojure.lang.Atom freq-hash & {depth :depth :or {depth 1}}]
  (let [data (words-from text)]
    (process-word-list data freq-hash :depth depth)))


; (defn gen-freq-data [^String from & {nthreads :nthreads :or {nthreads 1}}]
;   (let [freq-hash (atom {})]
;     (dothreads
;       (fn [] (apply process-text [from freq-hash]) :threads nthreads))
;     @freq-hash))





(defn ^String make-sentence
  [freq-hash
    & {partial-sentence :partial-sentence
       depth :depth
       :or {partial-sentence (get-first-words freq-hash) depth 1}}]
  (if (last-of-sentence? (last partial-sentence))
    (clojure.string/join " " partial-sentence)
    (recur freq-hash
           {:partial-sentence
            (conj partial-sentence
                  (get-word (subvec partial-sentence (- (count partial-sentence) depth)) freq-hash))
           :depth
            depth})))




