(ns clojure-noob.tests
  (:use clojure.test
        clojure-noob.algos))



(def ^:private test-data
  "The cat is happy. The dog is sad. I am retarded.")

(def ^:private depth1-chain
  {["The"] ["cat" "dog"]
   ["cat"] ["is"]
   ["dog"] ["is"]
   ["is"] ["happy." "sad."]
   ["sad."] ["I"]
   ["happy."] ["The"]
   ["I"] ["am"]
   ["am"] ["retarded."]
   :starter-index #{"The" "I"}})

(def ^:private depth3-chain
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
  (is (string? (#'clojure-noob.algos/get-word ["The"] depth1-chain)))
  (is (string? (#'clojure-noob.algos/get-word ["not-in-chain"] depth1-mchain)))
  (is (string? (#'clojure-noob.algos/get-word ["The" "cat" "is"] depth3-chain)))
  (is (string? (#'clojure-noob.algos/get-word ["not" "in" "chain"] depth3-mchain))))

(deftest test-update-freq-hash
  (let [key ["I" "am"]
        hash
        (#'clojure-noob.algos/update-freq-hash {key ["retarded."]} ["I" "am"] "cool.")]
    (is (= 1 (count hash)))
    (is (= (hash ["I" "am"]) ["retarded." "cool."]))))

(deftest test-get-first-words
  (is (= 1 (count (#'clojure-noob.algos/get-first-words depth1-chain))))
  (is (= 3 (count (#'clojure-noob.algos/get-first-words depth3-chain)))))

(deftest test-index-start-words
  (let [hash
        (#'clojure-noob.algos/index-start-words {} ["i" "am" "sofa" "king" "we" "todd." "did"])]
    (is (= (hash :starter-index) #{"did"}))))

(deftest test-process-text
  (let [depth1 (atom {}) depth3 (atom {})]
    (process-text test-data depth1 :depth 1)
    (process-text test-data depth3 :depth 3)
    (is (= depth1-chain @depth1))
    (is (= depth3-chain @depth3))))


(import '(java.util.concurrent Executors)
        '(java.util.concurrent TimeUnit))

(def pool (Executors/newFixedThreadPool
   (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(deftest test-add-word-threadsafe
  (let [chain (atom {})]
    (dotimes [t 10]
      (.submit pool #(dotimes [e 100] (add-word ["i"] "word" chain))))
    (.shutdown pool)
    (.awaitTermination pool 3 TimeUnit/SECONDS)
    (is (= (count (@chain ["i"])) 1000))))


