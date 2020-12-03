(ns core
  (:require
   [visualise.core :as v]
   [graph.core :as g]
   ;; [shams.priority-queue :as pq]
   ))

;; TODO:
;; - tests for all fns

;; The eccentricity of a vertex v is defined as the greatest distance between v and any other vertex.
;; The radius of a graph is the minimum eccentricity of any vertex in a graph.
;; The diameter of a graph is the maximum eccentricity of any vertex in a graph.

(defn G
  " N - size of generated graph
      S - sparseness (number of directed edges actually; from N-1 to N(N-1)/2)
      Returns: simple connected graph G(n,s) with N vertices and S edges "
  [N S]
  (g/densify-oriented (g/rand-graph :naive N) N S 10))

(defn D
  "Calculates shortest path from start to target vertex in graph. Returns a map
  with path and weight keys, or nil if no path is found."
  [graph start target]
  (g/shortest-path graph start target))

(defn eccentricity [graph vertex]
  (let  [vertices (g/dijkstra graph vertex)]
    (apply max (map :weight (vals vertices)))))

(defn eccentricities [graph]
  (map (partial eccentricity graph) (range (count graph))))

(defn radius [graph]
  (apply min (eccentricities graph)))

(defn diameter [graph]
  (apply max (eccentricities graph)))

;; DEBUG
(comment
  (let [size 10
        graph (G 10 (g/max-density size))]
    (println "Eccentricity: " (eccentricity graph 0))
    (println "Eccentricities: " (eccentricities graph))
    (println "Radius: " (radius graph))
    (println "Diameter: " (diameter graph))))

(comment
  (let [size 5
        graph (G size (g/max-density size))]
    (v/print-matrix graph)
    (println (D graph 0 4))
    (println (D graph 1 2))
    (println (D graph 0 4))
    (v/show graph {:directed? true})
    nil))

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
