(ns graph.core
  (:require
   [clojure.data.priority-map :as p]
   [clojure.set :as set]))

(defn make-matrix
  "Returns a 2 dimensional vector of size by size with every location set to v"
  [size v]
  (let [row (into [] (repeat size v))]
    (into [] (repeat size row))))

(defn make-nil-matrix
  "Returns a matrix of size by size with every location set to nil"
  [size]
  (make-matrix size nil))

(defn make-complete-matrix
  "Returns a matrix of size by size with every location set to 1, and diagonal set to nil"
  [size]
  (reduce
   (fn [m i] (assoc-in m [i i] nil))
   (make-matrix size 1) (range size)))

(defn to-adj-map
  "Returns a map of vertex to adjecent vertices as described by graph. An adjecent
  vertex is represented by a (vertex weight) tuple, but can be replaced by a
  custom value by passing in entry-fn which receives this tuple. "
  ([graph] (to-adj-map graph identity))
  ([adj-matrix entry-fn]
   (loop [adj-matrix adj-matrix adj-map {} i 0]
     (if (seq adj-matrix)
       (recur
        (rest adj-matrix)
        (assoc adj-map i (keep-indexed #(when %2 (entry-fn [%1 %2]))
                                       (first adj-matrix)))
        (inc i))
       adj-map))))

(defn to-adj-matrix
  "Returns an adjecent matrix of size by size derived from adj-map. Pass in
  entry-fn to parse a custom adjecent vertex value, which should return a
  standard (vertex weigh) tuple."
  ([adj-map size] (to-adj-matrix adj-map size identity))
  ([adj-map size entry-fn]
   (loop [adj-map adj-map adj-matrix (make-nil-matrix size)]
     (if (seq adj-map)
       (let [[start-vertex target-vertices] (first adj-map)
             adj-matrix (reduce (fn [adj-matrix target-vertex]
                                  (let [[target-vertex weight] (entry-fn target-vertex)]
                                    (assoc-in adj-matrix [start-vertex target-vertex] weight)))
                                adj-matrix target-vertices)]
         (recur (rest adj-map) adj-matrix))
       adj-matrix))))

(defn add-edge
  "Adds an edge to matrix"
  ([matrix edge] (add-edge matrix edge 1))
  ([matrix edge weight]
   (assoc-in matrix edge weight)))

(defn edges->adj-matrix
  "Returns an adjecency size by size matrix built from edges."
  [size edges]
  (reduce add-edge (make-nil-matrix size) edges))

(defn rand-graph-naive
  "Returns smallest possible (size -1) vector edges that together form a
   (slightly biased) connected tree spanning size nodes."
  [size]
  (loop [vertex 1 edges [] vertices [0]]
    (if (< vertex size)
      (recur (inc vertex)
             (conj edges [vertex (rand-nth vertices)])
             (conj vertices vertex))
      edges)))

(defn rand-graph-aldous-broder
  " Returns smallest possible (size -1) vector edges that together form a uniform
  connected tree spanning size nodes. adj-map should be a map of
  vertex to adjecent vertices and span size nodes"
  [adj-map size]
  (loop [vertex (rand-int size)
         edges []
         unvisited (disj (set (range size)) vertex)]
    (if (seq unvisited)
      (let [next-vertex (rand-nth (adj-map vertex))]
        (if (unvisited next-vertex)
          (recur next-vertex
                 (conj edges [vertex next-vertex])
                 (disj unvisited next-vertex))
          (recur next-vertex edges unvisited)))
      edges)))

;; Wilson's algorithm cuts out all loops while doing the random walk. But
;; actually you only need to break the loop, which is what the zipmap does.
(defn rand-graph-wilson
  " Returns smallest possible (size -1 ) vector edges that together form a uniform
  connected tree spanning size nodes. adj-map should be a map of vertex to
  adjecent vertices and span size nodes"
  [adj-map size]
  (loop [edges []
         unvisited (disj (set (range size)) (rand-int size))]
    (if (seq unvisited)
      (let [vertex (rand-nth (into [] unvisited))
            walk (iterate (comp rand-nth adj-map) vertex)
            new-edges (zipmap (take-while unvisited walk) (next walk))]
        (recur (apply conj edges (seq new-edges))
               (reduce disj unvisited (-> new-edges seq flatten set))))
      edges)))

(defmulti rand-graph
  "Returns a vector of (size - 1) edges together forming a connected
   tree spanning size nodes."
  (fn [method size]
    method))

(defmethod rand-graph :default [_ size]
  (rand-graph-naive size))

(defmethod rand-graph :naive [_ size]
  (rand-graph-naive size))

(defmethod rand-graph :random-walk [_ size]
  (rand-graph-aldous-broder (to-adj-map (make-complete-matrix size) first) size))

(defmethod rand-graph :wilson [_ size]
  (rand-graph-wilson (to-adj-map (make-complete-matrix size) first) size))

(defn get-random-subset
  "Returns a random subset of s of size n"
  [s n]
  (take n (shuffle s)))

(defn max-density
  "Returns maximum number of edges of an oriented simple directed connected graph
  with n vertices"
  [n]
  (* (/ (dec n) 2) n))

(defn min-density
  "Returns minimum number of edges of an oriented simple directed connected graph
  with n vertices"
  [n]
  (- n 1))

(defn get-unset-edges [matrix]
  (let [size (count matrix)]
    (for [i (range size)
          j (range (inc i) size)
          :when (and (not= i j) (nil? (get-in matrix [i j])))]
      [i j])))

(defn densify-oriented
  "Returns a size by size matrix of an oriented simple directed connected graph
  with specified density from edges that describe a tree spanning a graph with
  size vertices"
  [edges size density]
  {:pre [(not (> density (max-density size)))
         (not (< density (min-density size)))]}
  (let [undirected-edges (map sort edges)
        matrix (edges->adj-matrix size undirected-edges)
        extra-edges (get-random-subset (get-unset-edges matrix)
                                       (- density (min-density size)))]
    matrix (->> (apply conj undirected-edges extra-edges)
                (map shuffle)
                (edges->adj-matrix size))))

;; Implemented from https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm#CITEREFDijkstra1959
;; function Dijkstra(Graph, source):
;; 2      dist[source] ← 0                           // Initialization
;; 3
;; 4      create vertex priority queue Q
;; 5
;; 6      for each vertex v in Graph:
;; 7          if v ≠ source
;; 8              dist[v] ← INFINITY                 // Unknown distance from source to v
;; 9              prev[v] ← UNDEFINED                // Predecessor of v
;; 10
;; 11         Q.add_with_priority(v, dist[v])
;; 12
;; 13
;; 14     while Q is not empty:                      // The main loop
;; 15         u ← Q.extract_min()                    // Remove and return best vertex
;; 16         for each neighbor v of u:              // only v that are still in Q
;; 17             alt ← dist[u] + length(u, v)
;; 18             if alt < dist[v]
;; 19                 dist[v] ← alt
;; 20                 prev[v] ← u
;; 21                 Q.decrease_priority(v, alt)
;; 22
;; 23     return dist, prev

;; TODO:
;; Instead of filling the priority queue with all nodes in the initialization
;; phase, it is also possible to initialize it to contain only source; then,
;; inside the if alt < dist[v] block, the decrease_priority becomes an
;; add_with_priority operation if the node is not already in the queue.[8]:198

;; Yet another alternative is to add nodes unconditionally to the priority queue
;; and to instead check after extraction that no shorter connection was found
;; yet. This can be done by additionally extracting the associated priority p
;; from the queue and only processing further if p ≤ dist[u] inside the while Q
;; is not empty loop.

;; These alternatives can use entirely array-based priority queues without
;; decrease-key functionality which have been found to achieve even faster
;; computing times in practice.[17]

(defn dijkstra
  "Calculates shortest paths from vertex to all other vertices in graph described
  by matrix. Returns map of vertices."
  [matrix vertex]
  ;; Initialisation
  (let [adj-map (to-adj-map matrix first)
        Q (->>
           (range (count matrix)) ;;list of vertex ids
           (reduce #(conj %1 %2 {:vertex %2 :weight ##Inf :parent -1}) []) ;;tracking table
           (apply p/priority-map-keyfn :weight)) ;; put it in priority queue
        Q (assoc-in Q [vertex :weight] 0)] ;;set weight to 0 on start vertex

    ;; Main loop over priority queue
    (loop [Q Q vertices {}]
      (if (seq Q)
        ;; Retrieve and remove best vertex
        (let [[best-vertex-id best-vertex] (peek Q)
              Q (pop Q)]
          ;; Update neighbors left in Q
          (recur (reduce
                  (fn [Q neighbor]
                    ;; Calculate alternive weight for neighbor
                    (let [alt-weight (+ (:weight best-vertex)
                                        (get-in matrix [best-vertex-id neighbor]))]
                      (cond-> Q
                        ;;See if weigth is reduced for neighbor
                        (< alt-weight (:weight (get Q neighbor)))
                        ;; If so decrease neighbor's weight
                        (assoc neighbor {:vertex neighbor :weight alt-weight :parent best-vertex-id}))))
                  Q
                  ;; Reduce Q over neighbors that are not in Q anymore
                  (set/intersection (set (adj-map best-vertex-id)) (set (keys Q))))

                 ;; Add best vertex to vertices
                 (assoc vertices best-vertex-id best-vertex)))
        vertices))))

(defn shortest-path
  "Calculates shortest path from start to target vertex in graph described by
  matrix. Returns a map with path and weight keys, or nil if no path is found."
  [matrix start target]
  (let [vertices (dijkstra matrix start)
        path (loop [{:keys [parent]} (vertices target) path [target]]
               (if (not= parent -1)
                 (recur (vertices parent) (conj path parent))
                 (reverse path)))]
    {:path (when (= (first path) start) path)
     :weight (:weight (vertices target))}))
