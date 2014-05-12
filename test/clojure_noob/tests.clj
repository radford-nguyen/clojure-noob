(ns clojure-noob.tests
  (:use clojure.test
        clojure-noob.core))



(def ^:private test-data
  "The cat is happy. The dog is sad. I am retarded.")

(def ^:private depth1-model
  {["The"] ["cat" "dog"]
   ["cat"] ["is"]
   ["dog"] ["is"]
   ["is"] ["happy." "sad."]
   ["sad."] ["I"]
   ["happy."] ["The"]
   ["I"] ["am"]
   ["am"] ["retarded."]
   :firsts #{"The" "I"}})

(def ^:private depth3-model
  {["The" "cat" "is"] ["happy."]
   ["cat" "is" "happy."] ["The"]
   ["is" "happy." "The"] ["dog"]
   ["happy." "The" "dog"] ["is"]
   ["The" "dog" "is"] ["sad."]
   ["dog" "is" "sad."] ["I"]
   ["is" "sad." "I"] ["am"]
   ["sad." "I" "am"] ["retarded."]
   :firsts #{"The" "I"}})

(deftest test-last-of-sentence?
  (is (last-of-sentence? "end."))
  (is (last-of-sentence? "end?"))
  (is (not (last-of-sentence? "some"))))

(deftest test-words-from
  (is (= 1 (count (words-from "One==Two34Three."))))
  (is (= 3 (count (words-from "One== Two34 Three.")))))

(deftest test-rand-word
  (is (string? (#'clojure-noob.core/rand-word depth1-model ["The"])))
  (is (string? (#'clojure-noob.core/rand-word depth1-model ["not-in-model"])))
  (is (string? (#'clojure-noob.core/rand-word depth3-model ["The" "cat" "is"])))
  (is (string? (#'clojure-noob.core/rand-word depth3-model ["not" "in" "model"]))))

(deftest test-update-model
  (let [key ["I" "am"]
        hash
        (#'clojure-noob.core/update-model {key ["retarded."]} ["I" "am"] "cool.")]
    (is (= 1 (count hash)))
    (is (= (hash ["I" "am"]) ["retarded." "cool."]))))

(deftest test-rand-lead
  (is (= 1 (count (#'clojure-noob.core/rand-lead depth1-model))))
  (is (= 3 (count (#'clojure-noob.core/rand-lead depth3-model)))))

(deftest test-index-start-words
  (let [hash
        (#'clojure-noob.core/index-start-words {} ["i" "am" "sofa" "king" "we" "todd." "did"])]
    (is (= (hash :firsts) #{"did"}))))

(deftest test-process-text
  (let [depth1 (process-text test-data :depth 1)
        depth3 (process-text test-data :depth 3)]
    
    (is (= depth1-model depth1))
    (is (= depth3-model depth3))))


(import '(java.util.concurrent Executors)
        '(java.util.concurrent TimeUnit))

(def pool (Executors/newFixedThreadPool
   (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(deftest test-add-word-threadsafe
  (let [model (atom {})]
    (dotimes [t 10]
      (.submit pool #(dotimes [e 100] (#'clojure-noob.core/add-word ["i"] "word" model))))
    (.shutdown pool)
    (.awaitTermination pool 3 TimeUnit/SECONDS)
    (is (= (count (@model ["i"])) 1000))))


