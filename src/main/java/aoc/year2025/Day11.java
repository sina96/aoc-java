package aoc.year2025;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class Day11
{
   private final List<String> input;

   //graph
   private final Map<String, Edge> edgeNameMap = new HashMap<>();

   public Day11(List<String> input) {

      this.input = input;
      parseTheInput(input);
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }


      long solvePartOne = solvePartOne();
      System.out.println("Part 1: " + solvePartOne);
      long solvePartTwo = solvePartTwo();
      System.out.println("Part 2: " + solvePartTwo);
   }

   // =========================
   // Part One: count paths you -> out
   // =========================

   private long solvePartOne() {
      Edge start = edgeNameMap.get("you");
      if (start == null) {
         throw new IllegalStateException("No node named 'you' in input.");
      }
      return countPathsToOut(start);
   }

   private long countPathsToOut(Edge start) {
      Map<Edge, Long> memo = new HashMap<>();
      Set<Edge> visiting = new HashSet<>();
      return dfsCount(start, memo, visiting);
   }

   private long dfsCount(Edge node, Map<Edge, Long> memo, Set<Edge> visiting) {
      if ("out".equals(node.name)) {
         return 1L;
      }

      Long cached = memo.get(node);
      if (cached != null) {
         return cached;
      }

      if (!visiting.add(node)) {
         throw new IllegalStateException("Cycle detected involving node: " + node.name);
      }

      long total = 0L;
      for (Edge next : node.outputEdges) {
         total += dfsCount(next, memo, visiting);
      }

      visiting.remove(node);
      memo.put(node, total);
      return total;
   }

   // =========================
   // Part Two: count paths svr -> out that visit BOTH dac and fft (any order)
   // =========================

   private long solvePartTwo() {
      Edge start = edgeNameMap.get("svr");
      if (start == null) {
         throw new IllegalStateException("No node named 'svr' in input.");
      }

      Map<Edge, long[]> memo = new HashMap<>();
      Map<Edge, boolean[]> visiting = new HashMap<>();

      boolean hasDac = "dac".equals(start.name);
      boolean hasFft = "fft".equals(start.name);

      return dfsPartTwo(start, hasDac, hasFft, memo, visiting);
   }

   private long dfsPartTwo(
         Edge node,
         boolean hasDac,
         boolean hasFft,
         Map<Edge, long[]> memo,
         Map<Edge, boolean[]> visiting
   ) {
      if ("dac".equals(node.name)) hasDac = true;
      if ("fft".equals(node.name)) hasFft = true;

      if ("out".equals(node.name)) {
         return (hasDac && hasFft) ? 1L : 0L;
      }

      int stateIndex = stateIndex(hasDac, hasFft);

      long[] memoArr = memo.computeIfAbsent(node, k -> {
         long[] a = new long[4];
         Arrays.fill(a, -1L);
         return a;
      });

      if (memoArr[stateIndex] != -1L) {
         return memoArr[stateIndex];
      }

      boolean[] visitArr = visiting.computeIfAbsent(node, k -> new boolean[4]);
      if (visitArr[stateIndex]) {
         throw new IllegalStateException(
               "Cycle detected while counting paths; node=" + node.name +
                     ", hasDac=" + hasDac + ", hasFft=" + hasFft
         );
      }
      visitArr[stateIndex] = true;

      long total = 0L;
      for (Edge next : node.outputEdges) {
         total += dfsPartTwo(next, hasDac, hasFft, memo, visiting);
      }

      visitArr[stateIndex] = false;
      memoArr[stateIndex] = total;
      return total;
   }

   /**
    * Encodes the "required-nodes-visited" state into a small index.
    *
    * Index meanings:
    *   0 = neither dac nor fft has been visited yet
    *   1 = dac has been visited, fft has NOT
    *   2 = fft has been visited, dac has NOT
    *   3 = both dac and fft have been visited
    *
    * This index is used to address per-node memoization arrays:
    *   memo.get(node)[stateIndex]
    */
   private int stateIndex(boolean hasDac, boolean hasFft) {
      if (hasDac) {
         return hasFft ? 3 : 1;
      } else {
         return hasFft ? 2 : 0;
      }
   }

   // =========================
   // Parsing
   // =========================

   private void parseTheInput(List<String> input) {
      edgeNameMap.clear();

      // First pass: head nodes
      for (String line : input) {
         String[] parts = line.split(": ");
         String fromName = parts[0].trim();
         edgeNameMap.computeIfAbsent(fromName, Edge::new);
      }

      // Second pass: outputs
      for (String line : input) {
         String[] parts = line.split(": ");
         String fromName = parts[0].trim();
         Edge from = edgeNameMap.get(fromName);

         List<String> outs = Arrays.stream(parts[1].trim().split("\\s+"))
               .filter(s -> !s.isBlank())
               .toList();

         for (String outName : outs) {
            Edge to = edgeNameMap.computeIfAbsent(outName, Edge::new);
            from.addOutputEdge(to);
         }
      }

   }

   static class Edge {
      String name;
      List<Edge> outputEdges;

      public Edge(String name)
      {
         this.name = name;
         outputEdges = new ArrayList<>();
      }

      public void addOutputEdge(Edge outputEdge)
      {
         outputEdges.add(outputEdge);
      }

      public void addOutputEdges(List<Edge> outputEdges){
         this.outputEdges.addAll(outputEdges);
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
            return true;
         if (!(o instanceof Edge edge))
            return false;
         return Objects.equals(name, edge.name);
      }

      @Override
      public int hashCode()
      {
         return Objects.hashCode(name);
      }

      @Override
      public String toString()
      {
         return "Edge{" + "name='" + name + '\'' + ", outputEdgesSize=" + outputEdges.size() + '}';
      }
   }
}