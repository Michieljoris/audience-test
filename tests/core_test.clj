(ns core-test
  (:require
   [clojure.test :refer [deftest is testing run-tests]]
   [core :as c]
   [visualise.core :as v]
   [graph.core :as g]))

(deftest test-make-matrix
  (is (= (g/make-matrix 3 1) [[1 1 1][1 1 1][1 1 1]]))
  (is (= (g/make-complete-matrix 3) [[nil 1 1] [1 nil 1] [1 1 nil]])))

(deftest test-converting
  (let [graph [[nil 1 1] [1 nil 1] [1 1 nil]]]
    (is (= (g/to-adj-map graph) '{0 ([1 1] [2 1]), 1 ([0 1] [2 1]), 2 ([0 1] [1 1])}))
    (is (= (g/to-adj-map graph first) '{0 (1 2), 1 (0 2), 2 (0 1)})))

  (is (= (g/to-adj-matrix '{0 (1 2), 1 (0 2), 2 (0 1)} 3 (fn [v] [v 1]))
         [[nil 1 1] [1 nil 1] [1 1 nil]]))
  (is (= (g/to-adj-matrix '{0 ([1 1] [2 1]), 1 ([0 1] [2 1]), 2 ([0 1] [1 1])} 3)
         [[nil 1 1] [1 nil 1] [1 1 nil]]))
  (is (= (g/edges->adj-matrix 3 [[0 1] [1 2]])
         [[nil 1 nil] [nil nil 1] [nil nil nil]])))

(defn seq-graph [d adj-map start]
  ((fn rec-seq [explored frontier]
     (lazy-seq
      (if (empty? frontier)
        nil
        (let [v (peek frontier)
              neighbors (adj-map v)]
          (cons v (rec-seq
                   (into explored neighbors)
                   (into (pop frontier) (remove explored neighbors))))))))
   #{start} (conj d start)))

(def seq-graph-dfs (partial seq-graph []))

(defn connected?
  "Checks whether there is a path between any two vertices of the undirected graph
  described by edges"
  [edges size]
  (let [edges (mapcat (fn [[v1 v2]] [[v1 v2] [v2 v1]]) edges)
        matrix (g/edges->adj-matrix size edges)
        adj-map (g/to-adj-map matrix first)
        vs (range size)]
    (apply = (set vs) (map #(set (seq-graph-dfs adj-map %)) vs))))

(defn test-graph [type size]
  (let [edges (g/rand-graph type size)]
    (is (= (count edges) (- size 1)))
    (is (connected? edges size))))

(deftest test-random-graph
  (test-graph :naive 10)
  (test-graph :naive 20)
  (test-graph :naive 30)

  (test-graph :rand-path 10)
  (test-graph :random-path 20)
  (test-graph :random-path 30)

  (test-graph :wilson 10)
  (test-graph :wilson 20)
  (test-graph :wilson 30)
  )

(deftest test-dijkstra
  (let [graph [[nil 2 nil nil 5] [nil nil 6 nil nil] [3 nil nil 2 9] [10 9 nil nil 3] [nil 1 nil nil nil]]]
    (is (= (c/D graph 0 4) '{:path (0 4), :weight 5}))
    (is (= (c/D graph 1 2) '{:path (1 2), :weight 6}))
    (is (= (c/D graph 0 4) '{:path (0 4), :weight 5}))))

(deftest test-graph-props
  (let [graph
        [[nil 2 nil nil 5] [nil nil 6 nil nil] [3 nil nil 2 9] [10 9 nil nil 3] [nil 1 nil nil nil]]]
    (is (= (c/eccentricity graph 0) 10))
    (is (= (c/eccentricities graph) '(10 11 5 10 10)))
    (is (= (c/radius graph) 5))
    (is (= (c/diameter graph) 11))))
