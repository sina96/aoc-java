package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;


public class Day20
{
   private final List<String> input;

   public Day20(List<String> input)
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

   private void solvePartOne()
   {

      char[][] grid = makeTheGrid(input);
      Node startNode = getCharacterPosition('S', grid).get();
      Node endNode = getCharacterPosition('E', grid).get();
      final Map<Node, Integer> dijkstraDistanceMap = dijkstra(startNode, grid);

      int totalNrSingleCheats = calculateTotalSingleCheats(grid, startNode, endNode, dijkstraDistanceMap);

      System.out.println("Part 1: "+ totalNrSingleCheats);
   }

   private void solvePartTwo()
   {

      char[][] grid = makeTheGrid(input);
      Node startNode = getCharacterPosition('S', grid).get();
      Node endNode = getCharacterPosition('E', grid).get();
      final Map<Node, Integer> dijkstraDistanceMap = dijkstra(startNode, grid);

      final int totalNrMultipleCheats = calculateTotalNrMultipleCheats(grid, startNode, endNode, dijkstraDistanceMap);

      System.out.println("Part 2: " + totalNrMultipleCheats);
   }


   private int calculateTotalSingleCheats(char[][] grid, Node startNode, Node endNode, Map<Node, Integer> dijkstraDistanceMap)
   {
      Node currentNode = startNode;
      Node prev = null;
      int total = 0;

      while (!currentNode.equals(endNode)){
         total += calculateNrSingleCheats(grid, currentNode, endNode, dijkstraDistanceMap, 100);
         List<Node> neighbors = getNeighbors(currentNode, grid);
         for(Node n: neighbors)
         {
            if(!n.equals(prev)){
               prev = currentNode;
               currentNode = n;
               break;
            }
         }
      }
      return total;
   }

   private int calculateNrSingleCheats(char[][] grid, Node currentNode, Node endNode, Map<Node, Integer> dijkstraDistanceMap,
         int minDiff)
   {
      if (currentNode.equals(endNode))
      {
         return 0;
      } else {
         final List<Node> shortcutPoints = getShortCuts(currentNode, grid);
         int total = 0;
         for (final Node shortcut : shortcutPoints) {
            if (dijkstraDistanceMap.get(shortcut) > dijkstraDistanceMap.get(currentNode)
                  && dijkstraDistanceMap.get(shortcut) - dijkstraDistanceMap.get(currentNode) - 2 >= minDiff)
            {
               total++;
            }
         }
         return total;
      }
      
   }

   /**
    * Calculates the total number of "multiple cheats" between a start and an end node in a grid.
    * This involves traversing the grid from the start to the end and evaluating possible shortcuts
    * at each step using helper methods.
    *
    * @param grid 2D array representing the grid.
    * @param startNode The starting node of the traversal.
    * @param endNode The target node of the traversal.
    * @param dijkstraDistanceMap Map containing precomputed shortest distances from each node to the end node.
    * @return The total number of "multiple cheats" found during the traversal.
    */
   private int calculateTotalNrMultipleCheats(char[][] grid, Node startNode, Node endNode, Map<Node, Integer> dijkstraDistanceMap) {
      // Tracks the previously visited node to avoid backtracking
      Node previous = null;
      // Starts traversal from the start node
      Node currentNode = startNode;
      // Variable to accumulate the total number of cheats
      int total = 0;

      // Traverse until the current node matches the end node
      while (!currentNode.equals(endNode)) {
         // Add the number of cheats possible from the current node
         total += calculateMaxNrCheats(currentNode, 20, 100, endNode, dijkstraDistanceMap, grid);

         // Get the neighbors of the current node
         final List<Node> neighbours = getNeighbors(currentNode, grid);

         // Iterate over neighbors to select the next node for traversal
         for (final Node neighbour : neighbours) {
            // Avoid revisiting the previous node
            if (!neighbour.equals(previous)) {
               // Update the previous node to the current one
               previous = currentNode;
               // Move to the selected neighbor
               currentNode = neighbour;
               break; // Exit the loop once the next node is chosen
            }
         }
      }
      // Return the total number of cheats found during the traversal
      return total;
   }

   /**
    * Calculates the maximum number of "cheats" (shortcut opportunities) available from a given node.
    * Evaluates potential shortcut opportunities by comparing normal and Manhattan distances
    * and checking if the improvement meets a minimum difference.
    *
    * @param currentNode The node from which shortcuts are evaluated.
    * @param maxDistance Maximum allowable Manhattan distance for shortcut evaluation.
    * @param minDistanceSaved Minimum improvement (difference) required for a valid shortcut.
    * @param endNode The target node of the traversal.
    * @param dijkstraDistanceMap Map containing precomputed shortest distances from each node to the end node.
    * @param grid 2D array representing the grid.
    * @return The number of valid shortcuts ("cheats") found from the current node.
    */
   private int calculateMaxNrCheats(Node currentNode, int maxDistance, int minDistanceSaved, Node endNode,
         Map<Node, Integer> dijkstraDistanceMap, char[][] grid) {
      // If the current node is the end node, there are no shortcuts possible
      if (currentNode.equals(endNode)) {
         return 0;
      } else {
         // Get potential shortcut nodes within the max allowable distance
         List<Node> endPositions = getShortcutsWithMaxDistance(currentNode, grid, dijkstraDistanceMap, maxDistance);

         // Counter for valid shortcuts
         int total = 0;

         // Evaluate each potential shortcut node
         for (Node newEndNode : endPositions) {
            // Calculate the difference in distances:
            // 1. Normal distance difference based on the Dijkstra map
            int originalDistanceDiff = dijkstraDistanceMap.get(endNode) - dijkstraDistanceMap.get(currentNode);

            // 2. Manhattan distance to the shortcut node
            int manhattanDistanceDiff = getManhattanDistance(currentNode, newEndNode);

            // Calculate the new distance considering the shortcut
            int newDistance = manhattanDistanceDiff + dijkstraDistanceMap.get(endNode) - dijkstraDistanceMap.get(newEndNode);

            // Check if the improvement (normal distance difference - new distance) meets the minimum threshold
            if (originalDistanceDiff - newDistance >= minDistanceSaved) {
               total++; // Count this shortcut as valid
            }
         }
         // Return the total number of valid shortcuts from the current node
         return total;
      }
   }

   private List<Node> getShortCuts(Node currentNode, char[][] grid)
   {
      final List<Node> shortCuts = new ArrayList<>();

      if (isReachable(currentNode.x, currentNode.y, grid)) {
         if (isWall(currentNode.x-1,currentNode.y, grid) &&
               isReachable(currentNode.x - 2, currentNode.y, grid)) {
            shortCuts.add(new Node(currentNode.x - 2, currentNode.y));
         }
         if (isWall(currentNode.x + 1, currentNode.y,grid) &&
               isReachable(currentNode.x + 2, currentNode.y, grid)) {
            shortCuts.add(new Node(currentNode.x + 2, currentNode.y));
         }
         if (isWall(currentNode.x, currentNode.y -1, grid) &&
               isReachable(currentNode.x, currentNode.y - 2, grid)) {
            shortCuts.add(new Node(currentNode.x, currentNode.y - 2));
         }
         if (isWall(currentNode.x, currentNode.y + 1, grid) &&
               isReachable(currentNode.x, currentNode.y + 2, grid)) {
            shortCuts.add(new Node(currentNode.x, currentNode.y + 2));
         }
      }

      return shortCuts;
   }

   /**
    * Retrieves a list of potential shortcut nodes that are within a specified maximum Manhattan distance
    * from the current node and satisfy certain conditions based on the provided distance map.
    *
    * @param currentNode The current node being evaluated for shortcuts.
    * @param grid 2D array representing the grid.
    * @param dijkstraDistanceMap Map containing precomputed shortest distances from each node to the end node.
    * @param maxDistance The maximum allowable Manhattan distance for a shortcut.
    * @return A list of nodes representing valid shortcut candidates.
    */
   private List<Node> getShortcutsWithMaxDistance(Node currentNode, char[][] grid, Map<Node, Integer> dijkstraDistanceMap, int maxDistance) {
      // Initialize a list to store potential shortcut neighbors
      final List<Node> neighbours = new ArrayList<>();

      // Iterate through all positions in the grid
      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[0].length; x++) {
            // Create a new node for the current grid position

            // Check if the new node meets the criteria for a shortcut:
            // 1. Its Manhattan distance from the current node is within the max distance.
            // 2. Its Manhattan distance from the current node is greater than 1 (to avoid trivial neighbors).
            // 3. It exists in the Dijkstra distance map (ensures it's a valid node with precomputed distance).
            // 4. Its Manhattan distance is less than the difference between its Dijkstra distance
            //    and the current node's Dijkstra distance (ensures the shortcut provides a real benefit).
            if (getManhattanDistance(currentNode, new Node(x, y)) <= maxDistance
                  && getManhattanDistance(currentNode, new Node(x, y)) > 1
                  && dijkstraDistanceMap.containsKey(new Node(x, y))
                  && getManhattanDistance(currentNode, new Node(x, y)) < dijkstraDistanceMap.get(new Node(x, y))
                  - dijkstraDistanceMap.get(currentNode)) {
               // Add the node to the list of valid neighbors
               neighbours.add(new Node(x, y));
            }
         }
      }

      // Return the list of shortcut neighbors
      return neighbours;
   }

   private char[][] makeTheGrid(final List<String> input) {
      final char[][] grid = new char[input.size()][input.get(0).length()];
      for (int y = 0; y < grid.length; y++) {
         final String line = input.get(y);
         for (int x = 0; x < grid[y].length; x++) {
            grid[y][x] = line.charAt(x);
         }
      }
      return grid;
   }

   private Optional<Node> getCharacterPosition(final char c, final char[][] grid)
   {
      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[y].length; x++) {
            if (grid[y][x] == c) {
               return Optional.of(new Node(x, y));
            }
         }
      }
      return Optional.empty();
   }

   private Map<Node,Integer> dijkstra(Node startNode, char[][] grid)
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
            int newDistance = currentDistance + 1;
            if(newDistance < distances.getOrDefault(n, Integer.MAX_VALUE))
            {
               distances.put(n, newDistance);
               queue.offer(n);
            }
         }
      }

      return distances;
   }

   private List<Node> getNeighbors(Node currentNode, char[][] grid)
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

   private int getManhattanDistance(Node currentNode, Node otherNode)
   {
      return Math.abs(currentNode.x - otherNode.x) + Math.abs(currentNode.y - otherNode.y);
   }

   private static boolean isReachable(int newX, int newY, char[][] grid)
   {
      int rows = grid.length;
      int cols = grid[0].length;

      return isInBounds(newX, newY, rows, cols) && grid[newY][newX] != '#';
   }

   private static boolean isWall(int newX, int newY, char[][] grid)
   {
      int rows = grid.length;
      int cols = grid[0].length;

      return isInBounds(newX, newY, rows, cols) && grid[newY][newX] == '#';
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

