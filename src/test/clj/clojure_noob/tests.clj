(ns clojure-noob.tests (:use clojure.test))



(def ^:private test-data
  "The cat is happy. The dog is sad. I am retarded.")

(def ^:private depth1-corpus
  {["The"] ["cat" "dog"]
   ["cat"] ["is"]
   ["dog"] ["is"]
   ["is"] ["happy." "sad."]
   ["sad."] ["I"]
   ["happy."] ["The"]
   ["I"] ["am"]
   ["am"] ["retarded."]
   :starter-index #{"The" "I"}})

(def ^:private depth3-corpus
  {["The" "cat" "is"] ["happy."]
   ["cat" "is" "happy."] ["The"]
   ["is" "happy." "The"] ["dog"]
   ["happy." "The" "dog"] ["is"]
   ["The" "dog" "is"] ["sad."]
   ["dog" "is" "sad."] ["I"]
   ["is" "sad." "I"] ["am"]
   ["sad." "I" "am"] ["retarded."]
   :starter-index #{"The" "I"}})

(deftest test-last-of-sentence?
         (is (last-of-sentence? "end."))
         (is (last-of-sentence? "end?"))
         (is (not (last-of-sentence? "some"))))

(deftest test-words-from
         (is (= 1 (count (words-from "One==Two34Three."))))
         (is (= 3 (count (words-from "One== Two34 Three.")))))

(deftest test-get-word
         (is (string? (get-word ["The"] depth1-corpus)))
         (is (string? (get-word ["not-in-corpus"] depth1-corpus)))
         (is (string? (get-word ["The" "cat" "is"] depth3-corpus)))
         (is (string? (get-word ["not" "in" "corpus"] depth3-corpus))))

(deftest test-update-freq-hash
         (let [key ["I" "am"]
               hash
               (update-freq-hash {key ["retarded."]} ["I" "am"] "cool.")]
           (is (= 1 (count hash)))
           (is (= (hash ["I" "am"]) ["retarded." "cool."]))))

(deftest test-get-first-words
         (is (= 1 (count (get-first-words depth1-corpus))))
         (is (= 3 (count (get-first-words depth3-corpus)))))

(deftest test-index-start-words
         (let [hash
               (index-start-words {} ["i" "am" "sofa" "king" "we" "todd." "did"])]
           (is (= (hash :starter-index) #{"did"}))))

(deftest test-process-text
         (let [depth1 (atom {}) depth3 (atom {})]
           (process-text test-data depth1 :depth 1)
           (process-text test-data depth3 :depth 3)
           (is (= depth1-corpus @depth1))
           (is (= depth3-corpus @depth3))))



(import '(java.util.concurrent Executors))

(def pool (Executors/newFixedThreadPool
             (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(deftest test-add-word-threadsafe
         (let [corpus (atom {})]
           (dotimes [t 10]
             (.submit pool #(dotimes [e 100] (add-word ["i"] "word" corpus))))
           (. Thread (sleep 3000))
           (is (= (count (@corpus ["i"])) 1000))))


