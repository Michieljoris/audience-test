(ns visualise.core
  (:require
   [graph.core :as g]
   [rhizome.viz :as r]))

  (defn print-matrix [matrix]
    (println)
    (doseq [row matrix]
      (doseq [v row] (print v ""))
      (println))
    matrix)

  (defn print-map [adj-map]
    (println)
    (doseq [[k v] adj-map]
      (println k ": " v))
    adj-map)

  (defn print-as-map
    "Takes a matrix and prints the adjecency map. Pass in entry-fn to customise the
  adjecent vertex values."
    ([matrix] (print-as-map matrix identity))
    ([matrix entry-fn]
     (let [adj-map (g/to-adj-map matrix entry-fn)]
       (print-map adj-map))
     matrix))

  (defn standard-rhizome-options [graph]
    {:filename "rhizome.png"
     :directed? false
     :vertical? true
     :edge->descriptor (fn [src dest] {:label (get-in graph [src dest])})
     :node->descriptor (fn [n] {:label n})})

  (defn visualise
    [f graph options]
    (let [adj-map (g/to-adj-map graph first)
          options (merge (standard-rhizome-options graph) options)]
      (apply f (keys adj-map) adj-map (-> options seq flatten)))
    graph)

  (defn save
    ([graph] (save graph (standard-rhizome-options graph)))
    ([graph options] (visualise r/save-graph graph options)))

  (defn show
    ([graph] (show graph (standard-rhizome-options graph)))
    ([graph options] (visualise r/view-graph graph options)))
