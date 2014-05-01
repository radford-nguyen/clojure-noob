(ns clojure-noob.algos
  "Contains core functions for modeling text using a Markov
  Chain and generating realistic-looking random text using
  that data. The Markov Chain is represented as a
  map {f s} where:
  
  f         = vector of words
  (count f) = Markov depth
  s         = vector of following words
  
  The vector s may contain duplicate words, which indicate
  that they appeared more than once following words f. Contrast
  this approach with storing the frequency information explicitly
  in a map.
  
  The chain also contains a vector of all the first words
  from any sentence in the text at:
  
  (chain :starter-index)")


(defn last-of-sentence? [word]
  (let [last-char (last word)]
    (or (= \. last-char) (= \? last-char))))

(defn words-from [s]
  (clojure.string/split s #"\s+"))


(defn- get-first-words [chain & {acc :acc :or {acc 0}}]
  "Returns a vector of first-words from a sentence, chosen
  at random from the data in the given chain."
  (let [words (rand-nth (keys (dissoc chain :starter-index)))
        word (first words)
        corpus-size (count chain)]
    (if (contains? (chain :starter-index) word)
      words
      (if (>= acc (* corpus-size corpus-size))
        ; avoid infinite looping
        ["ERROR: You have seeded the generator with data that does not exist in the corpus."]
        (recur chain {:acc (inc acc)})))))


(defn- get-word [words, chain]
  "Given a vector of words, produces the next word probabilistically
  from the frequency hash. If words does not exist in
  chain, then a random word is returned."
  (if-let [next-words (chain words)]
    (rand-nth next-words)
    (rand-nth (rand-nth (keys (dissoc chain :starter-index))))))


(defn- update-chain [chain, first-words, next-word]
  (if-let [list-of-next-words (chain first-words)]
    (assoc chain first-words (conj list-of-next-words next-word))
    (assoc chain first-words [next-word])))


(defn- index-start-words [chain, prev-words]
  "Returns a new markov chain with the starter-index
  updated based on prev-words"
  (if-let [starter-words (chain :starter-index)]
    (assoc chain :starter-index (conj starter-words (last prev-words)))
    (assoc chain :starter-index #{(last prev-words)})))









(defn add-word [prev-words, next-word, ^clojure.lang.Atom chain]
  (swap! chain update-chain prev-words next-word))


(defn process-word-list
  
  [word-list, ^clojure.lang.Atom chain
   & {store-first :store-first depth :depth :or {depth 1}}]

  (let [prev-words (subvec word-list 0 depth)]
    (if store-first
      (swap! chain index-start-words prev-words))
    (if (< depth (count word-list))
      (let [_ (add-word prev-words (nth word-list depth) chain)
            store-first (last-of-sentence? (last prev-words))]
        (recur (subvec word-list 1)
               chain
               {:store-first store-first :depth depth})))))


(defn process-text
  "Processes the given text and stores its data in the
  given chain Atom. The atom is a map (m) that holds the markov
  data indexed from the text."
  [^String text, ^clojure.lang.Atom chain & {depth :depth :or {depth 1}}]
  (let [data (words-from text)]
    (process-word-list data chain :depth depth)))

; (defn gen-freq-data [^String from & {nthreads :nthreads :or {nthreads 1}}]
;   (let [chain (atom {})]
;     (dothreads
;       (fn [] (apply process-text [from chain]) :threads nthreads))
;     @chain))

(defn make-sentence
  "Returns a random sentence, generated from the given chain Atom."
  [chain
    & {partial-sentence :partial-sentence
       depth :depth
       :or {partial-sentence (get-first-words chain) depth 1}}]
  (if (last-of-sentence? (last partial-sentence))
    (clojure.string/join " " partial-sentence)
    (recur chain
           {:partial-sentence
            (conj partial-sentence
                  (get-word (subvec partial-sentence (- (count partial-sentence) depth)) chain))
           :depth
            depth})))




