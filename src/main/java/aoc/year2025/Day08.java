package aoc.year2025;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class Day08
{

   private final List<String> input;
   private List<Junction> junctions;

   public Day08(List<String> input) {
      if (input == null || input.isEmpty()) {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      this.input = input;
      parseTheJunctions();
   }

   public void solve() {

      // 1) Generate all possible connections (edges)
      List<Edge> edges = populatesEdges();
      // 2) Sort by distance ascending
      Collections.sort(edges);

      System.out.println("Part 1: " + solvePartOne(edges, 1000));
      System.out.println("Part 2: " + solvePartTwo(edges));
   }

   private String solvePartOne(List<Edge> edges, int iter)
   {
      // 3) Assign each junction a unique index for DSU
      Map<Junction, Integer> index = new HashMap<>();
      for (int i = 0; i < junctions.size(); i++) {
         index.put(junctions.get(i), i);
      }

      // 4) Initialize DSU (each junction is its own circuit)
      DSU dsu = new DSU(junctions.size());

      // 5) Add the first "iter" shortest connections
      for (int i = 0; i < Math.min(iter, edges.size()); i++) {
         Edge e = edges.get(i);

         int a = index.get(e.head);
         int b = index.get(e.tail);

         // Merge circuits if not already connected
         dsu.union(a, b);
      }

      // 6) Count how many junctions are in each circuit
      Map<Integer, Integer> componentSizes = new HashMap<>();
      for (int i = 0; i < junctions.size(); i++) {
         int root = dsu.find(i);
         componentSizes.merge(root, 1, Integer::sum);
      }

      // 7) Find the three largest circuits
      List<Integer> sizes = new ArrayList<>(componentSizes.values());
      sizes.sort(Comparator.reverseOrder());

      // 8) Multiply the three largest sizes
      long product = 1;
      for (int i = 0; i < Math.min(3, sizes.size()); i++) {
         product *= sizes.get(i);
      }

      return String.valueOf(product);
   }

   private String solvePartTwo(List<Edge> edges) {

      // 2) Map each Junction to an index for DSU operations
      Map<Junction, Integer> index = new HashMap<>();
      for (int i = 0; i < junctions.size(); i++) {
         index.put(junctions.get(i), i);
      }

      // 3) DSU starts with N separate components (circuits)
      DSU dsu = new DSU(junctions.size());

      // 4) Add edges until the whole set becomes one component
      for (Edge e : edges) {
         int a = index.get(e.head);
         int b = index.get(e.tail);

         // union(...) returns true only if it actually merged two circuits
         if (dsu.union(a, b)) {
            // If we just formed a single global circuit, this is the last required connection
            if (dsu.components() == 1) {
               long product = (long) e.head.x * (long) e.tail.x;
               return String.valueOf(product);
            }
         }
      }

      throw new IllegalStateException("Failed to connect all junctions (unexpected).");
   }

   private List<Edge> populatesEdges() {
      List<Edge> edges = new ArrayList<>();
      for (int i = 0; i < junctions.size(); i++) {
         Junction jHead = junctions.get(i);
         for (int j = i + 1; j < junctions.size(); j++) {
            Junction jTail = junctions.get(j);
            edges.add(new Edge(jHead, jTail));
         }
      }
      return edges;
   }

   private void parseTheJunctions()
   {
      junctions = input.stream()
            .map(line -> line.split(","))
            .map(t -> new Junction(
                  Integer.parseInt(t[0]),
                  Integer.parseInt(t[1]),
                  Integer.parseInt(t[2])
            ))
            .collect(Collectors.toList());
   }

   static class Junction {
      int x,y,z;

      public Junction(int x, int y, int z)
      {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public long squaredDistanceFrom(Junction other) {
         long dx = (long) x - other.x;
         long dy = (long) y - other.y;
         long dz = (long) z - other.z;
         return dx * dx + dy * dy + dz * dz;
      }


      public int getX()
      {
         return x;
      }

      public void setX(int x)
      {
         this.x = x;
      }

      public int getY()
      {
         return y;
      }

      public void setY(int y)
      {
         this.y = y;
      }

      public int getZ()
      {
         return z;
      }

      public void setZ(int z)
      {
         this.z = z;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
            return true;
         if (!(o instanceof Junction junction))
            return false;
         return x == junction.x && y == junction.y && z == junction.z;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(x, y, z);
      }
   }

   static class Edge implements Comparable<Edge> {

      Junction head;
      Junction tail;

      // Squared distance is enough for ordering and avoids sqrt errors
      long distance2;

      public Edge(Junction head, Junction tail) {
         this.head = head;
         this.tail = tail;
         this.distance2 = head.squaredDistanceFrom(tail);
      }

      // Sort edges from shortest to longest
      @Override
      public int compareTo(Edge o) {
         return Long.compare(this.distance2, o.distance2);
      }
   }

   /*
   DSU (also called Union–Find) is a data structure used to efficiently manage groups of connected items.

   In your problem’s language:
	•	Each junction box starts in its own circuit
	•	When you connect two junction boxes, their circuits merge
	•	You frequently need to ask:
	•	“Are these two junctions already in the same circuit?”
	•	“Merge these two circuits if they aren’t”

   DSU is designed exactly for this.

    */
   static class DSU {

      // parent[i] = parent of Edge i
      // if parent[i] == i, then i is the root of its circuit
      int[] parent;

      // Used to keep trees shallow (performance optimization)
      int[] rank;

      int componentCount;

      DSU(int n) {
         parent = new int[n];
         rank = new int[n];
         componentCount = n;
         // Initially, each junction is its own circuit
         for (int i = 0; i < n; i++) {
            parent[i] = i;
         }
      }

      // Find the root of the circuit containing x
      int find(int x) {
         // Path compression: flattens the tree
         if (parent[x] != x) {
            parent[x] = find(parent[x]);
         }
         return parent[x];
      }

      // Connect the circuits containing a and b
      boolean union(int a, int b) {
         int ra = find(a);
         int rb = find(b);
         if (ra == rb) return false;

         if (rank[ra] < rank[rb]) {
            parent[ra] = rb;
         } else if (rank[ra] > rank[rb]) {
            parent[rb] = ra;
         } else {
            parent[rb] = ra;
            rank[ra]++;
         }

         componentCount--; // we merged two components into one
         return true;
      }

      int components() {
         return componentCount;
      }
   }


}