(ns core
  (:require
   [visualise.core :as v]
   [graph.core :as g]
   ;; [shams.priority-queue :as pq]
   ))

;; TODO:
;; - random weight
;; - eccentricity, radius, diameter
;; - tests for all fns

(defn G
  " N - size of generated graph
      S - sparseness (number of directed edges actually; from N-1 to N(N-1)/2)
      Returns: simple connected graph G(n,s) with N vertices and S edges "
  [N S]
  (g/densify-oriented (g/rand-graph :naive N) N S))

(defn D
  "Calculates shortest path from start to target vertex in graph. Returns a map
  with path and weight keys, or nil if no path is found."
  [graph start target]
  (g/shortest-path graph start target))

;; DEBUG
(comment
  (let [size 5
        graph (G size (inc (g/min-density size)))]
    (v/print-matrix graph)
    (v/show graph {:directed? true})))

(comment
  (do
    (time (g/rand-graph :naive 1000))
    (time (g/rand-graph :random-walk 1000))
    (time (g/rand-graph :wilson 1000))
    nil
    ))

(comment
  (let [matrix [[nil nil 1 1] [1 nil nil nil] [nil 1 nil nil] [nil nil nil nil]]]
    (println "=======================")
    (v/print-as-map matrix)
    (println (g/shortest-path matrix 0 1))
    (v/show matrix {:directed? true})
    ))
