## Howto

Run repl with

    clj -r

The function G and D are in the core namespace:

    (require '[core :refer [G D]]

    (G 5 4) ;;returns a matrix of a random graph
    => [[nil 1 1 1 nil] [nil nil nil nil nil] [nil nil nil nil nil] [nil 1 nil nil nil] [nil 1 nil nil nil]]

    (D (G 5 4) 0 1)  ;; returns shortest path from vertex 0 to 1 or nil
    => {:path (0 1), :weight 1}
    
Visualise graphs with:
    
    (v/print-matrix (G 5 4))
    (v/print-map (G 5 4))
    (v/show (G 5 4) {:directed? true})
    (v/save (G 5 4))
    
## Notes 
- The graph functions are in the graph.core namespace, visualisation functions
  in the visualise.core namespace
- Internal representations of graphs are a mix of adjacency map/matrix and just
  a collection of edges, whatever is most useful. The graph.core namespace
  includes its conversion functions.
- A simple directed graph can include edges in both directions between the same
two vertices but because the exercise sets the limit of sparsity to N(N-1)/2 the
assumption is made that the intention was an _oriented_ simple directed graph. 
- The prioriy queue version of Dijkstra's algorithm was implemented from its
  wikipedia page. I left the further optimisations for a future exercise, but
  are noted in the code.
- There are three implementations of a random graph creation algorithm. The
  naive version is slightly biased, so I included two more truly random
  algorithms. This seems to be a hard problem, and these last two are
  significantly slower than the naive version, though wilson beats random path.
   
   
    (do
      (time (g/rand-graph :naive 1000))
      (time (g/rand-graph :random-walk 1000))
      (time (g/rand-graph :wilson 1000))
      nil)
    "Elapsed time: 2.777833 msecs"
    "Elapsed time: 503.332304 msecs"
    "Elapsed time: 132.008273 msecs"
