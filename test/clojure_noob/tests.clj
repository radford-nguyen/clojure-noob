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
  (let [rand-word #'clojure-noob.core/rand-word]
    (is (string? (rand-word depth1-model ["The"])))
    (is (string? (rand-word depth1-model ["not-in-model"])))
    (is (string? (rand-word depth3-model ["The" "cat" "is"])))
    (is (string? (rand-word depth3-model ["not" "in" "model"])))))

(deftest test-update-model
  (let [update-model #'clojure-noob.core/update-model
        lead ["I" "am"]
        model (update-model {} lead "retarded.")
        model (update-model model lead "cool.")]
    (is (= 1 (count model)))
    (is (= (model lead) ["retarded." "cool."]))))

(deftest test-rand-lead
  (let [rand-lead #'clojure-noob.core/rand-lead]
    (is (= 1 (count (rand-lead depth1-model))))
    (is (= 3 (count (rand-lead depth3-model))))))

(deftest test-add-first-word
  (let [add-first-word #'clojure-noob.core/add-first-word
        model (add-first-word {} "did")]
    (is (= (model :firsts) #{"did"}))))

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


