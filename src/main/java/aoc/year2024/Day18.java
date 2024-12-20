package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;


public class Day18
{
   private final List<String> input;

   public Day18(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      solvePartOne();
      solvePartTwo();
   }

   private void solvePartTwo()
   {
      String firstByte = findFirstBlockingByteTernarySearch(input);

      System.out.println("Part Two: " + firstByte);
   }

   private void solvePartOne()
   {
      Node startNode = new Node(0, 0);
      Node endNode = new Node(70, 70);

      int[][] grid = makeTheGrid(71, 1024, input);
      final Map<Node, Integer> totalDistanceMap = dijkstra(startNode, grid);

      int lowestDistanceToEnd = totalDistanceMap.get(endNode);

      System.out.println("Part 1: "+ lowestDistanceToEnd);
   }

   private int[][] makeTheGrid(final int size, final int nrBytes, final List<String> input) {
      final int[][] grid = new int[size][size];
      for (int i = 0; i <= nrBytes; i++) {
         final String line = input.get(i);
         int x = Integer.parseInt(line.split(",")[0]);
         int y = Integer.parseInt(line.split(",")[1]);
         grid[y][x] = 1;
      }
      return grid;
   }

   private Map<Node,Integer> dijkstra(Node startNode, int[][] grid)
   {
      final PriorityQueue<Node> queue = new PriorityQueue<>();
      queue.offer(startNode);
      final Map<Node, Integer> distances = new HashMap<>();

      distances.put(startNode, 0);
      while (!queue.isEmpty())
      {
         final Node currentNode = queue.poll();
         final int currentDistance = distances.get(currentNode);
         final List<Node> neighbors = getNeighbors(currentNode, grid);
         for(Node n : neighbors)
         {
            int newDistannce = currentDistance + 1;
            if(newDistannce < distances.getOrDefault(n, Integer.MAX_VALUE))
            {
               distances.put(n, newDistannce);
               queue.offer(n);
            }
         }
      }

      return distances;
   }

   private String findFirstBlockingByteTernarySearch(final List<String> input) {
      final Node start = new Node(0, 0);
      final Node end = new Node(70, 70);

      int left = 1025; // Start after 1024
      int right = input.size() - 1; // End at the last byte

      while (left < right) {
         // Divide into three parts
         int mid1 = left + (right - left) / 3;
         int mid2 = right - (right - left) / 3;

         // Check paths for mid1 and mid2
         boolean pathAtMid1 = hasPath(start, end, makeTheGrid(71, mid1, input));
         boolean pathAtMid2 = hasPath(start, end, makeTheGrid(71, mid2, input));

         if (!pathAtMid1) {
            // If no path exists at mid1, the blocking byte is <= mid1
            right = mid1;
         } else if (!pathAtMid2) {
            // If path exists at mid1 but not at mid2, blocking byte is between mid1 and mid2
            left = mid1 + 1;
            right = mid2;
         } else {
            // If paths exist at both mid1 and mid2, blocking byte is > mid2
            left = mid2 + 1;
         }
      }

      return input.get(left);
   }

   private boolean hasPath(Node start, Node end, int[][] grid) {
      Map<Node, Integer> distances = dijkstra(start, grid);
      return distances.get(end) != null;
   }

   private List<Node> getNeighbors(Node currentNode, int[][] grid)
   {
      final List<Node> neighbours = new ArrayList<>();

      if (isReachable(currentNode.x - 1, currentNode.y, grid)) {
         neighbours.add(new Node(currentNode.x - 1, currentNode.y));
      }
      if (isReachable(currentNode.x + 1, currentNode.y, grid)) {
         neighbours.add(new Node(currentNode.x + 1, currentNode.y));
      }
      if (isReachable(currentNode.x, currentNode.y - 1, grid)) {
         neighbours.add(new Node(currentNode.x, currentNode.y - 1));
      }
      if (isReachable(currentNode.x, currentNode.y + 1, grid)) {
         neighbours.add(new Node(currentNode.x, currentNode.y + 1));
      }
      return neighbours;
   }

   private static boolean isReachable(int newX, int newY, int[][] grid)
   {
      int rows = grid.length;
      int cols = grid[0].length;

      return isInBounds(newX, newY, rows, cols) && grid[newY][newX] != 1;
   }

   private static boolean isInBounds(int newX, int newY, int rows, int cols)
   {
      return newX >= 0 && newY >= 0 && newX < cols && newY < rows;
   }

   static class Node implements Comparable<Node> {
      int x, y;
      String uniqueKey; //"x-y"

      Node(int x, int y) {
         this.x = x;
         this.y = y;
         this.uniqueKey = x+"-"+y;
      }

      @Override
      public int compareTo(Node other) {
         if (this.y != other.y) {
            return Integer.compare(this.y, other.y);
         } else {
            return Integer.compare(this.x, other.x);
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!(obj instanceof Node other)) return false;
         return this.x == other.x && this.y == other.y;
      }

      @Override
      public int hashCode() {
         return Objects.hash(x, y);
      }

   }

}

