package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;


public class Day16
{
   private final List<String> input;
   private static final String[] directions = {"N", "E", "S", "W"};

   public Day16(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      solvePartOneAndTwo();
   }

   private void solvePartOneAndTwo()
   {
      char[][] grid = makeTheGrid();

      Node startNode = getCharacterPosition('S', grid, directions[1]).get();
      List<Node> possibleEndNodes = getPossibleEndNodes(grid);


      final Map<Node, Integer> allNodesEvaluated = performDijkstra(grid, startNode);
      int lowestDistance = possibleEndNodes.stream().mapToInt(allNodesEvaluated::get).min().getAsInt();
      System.out.println("Part 1: " + lowestDistance);

      Node realEndNode = allNodesEvaluated.entrySet().stream().filter(e -> e.getValue() == lowestDistance).findFirst().get().getKey();
      final Set<String> allPathTilesOnShortestPaths = new HashSet<>();
      backTrackAndFindPossibleTilesOnShortestPaths(realEndNode, startNode, allNodesEvaluated, grid, allPathTilesOnShortestPaths);
      System.out.println("Part 2: " + allPathTilesOnShortestPaths.size());
   }

   private Map<Node, Integer> performDijkstra(char[][] grid, Node startNode)
   {
      final PriorityQueue<Node> queue = new PriorityQueue<>();
      queue.offer(startNode);
      final Map<Node, Integer> distances = new HashMap<>();
      distances.put(startNode, 0);
      while (!queue.isEmpty()) {
         final Node current = queue.poll();
         final int currentDistance = distances.get(current);

         evaluateAndAddDistanceFromNeighbors(grid, current, currentDistance, distances, queue);
      }
      return distances;
   }

   private static void evaluateAndAddDistanceFromNeighbors(char[][] grid, Node current, int currentDistance, Map<Node, Integer> distances,
         PriorityQueue<Node> queue)
   {
      final Optional<Node> directNeighbor = getDirectNeighborAhead(current, grid);
      final List<Node> turningsOfThisNode = getTurningsOfThisNode(current, grid);

      if(directNeighbor.isPresent())
      {
         final Node neighbor = directNeighbor.get();
         int newDistance = currentDistance;
         newDistance++;
         if (newDistance < distances.getOrDefault(neighbor, Integer.MAX_VALUE))
         {
            distances.put(neighbor, newDistance);
            queue.offer(neighbor);
         }
      }

      for(final Node tn : turningsOfThisNode) {
         int newDistance = currentDistance;
         newDistance += 1000;
         if(newDistance < distances.getOrDefault(tn, Integer.MAX_VALUE)) {
            distances.put(tn, newDistance);
            queue.offer(tn);
         }
      }
   }

   private static List<Node> findAndAddBacktrackTiles(Node currentNode, Map<Node, Integer> allNodesEvaluated, char[][] grid, int distanceToStart)
   {
      List<Node> nextPositions = new ArrayList<>();
      // look at directly neighbor behind
      final Optional<Node> oppositeNeighbor = getDirectNeighborBehind(currentNode, grid);
      if(oppositeNeighbor.isPresent() && allNodesEvaluated.get(oppositeNeighbor.get()) < distanceToStart){
         nextPositions.add(oppositeNeighbor.get());
      }

      // look at turns of this node
      final List<Node> turningsOfThisNode = getTurningsOfThisNode(currentNode, grid);
      for(Node tn : turningsOfThisNode){
         if(allNodesEvaluated.getOrDefault(tn, Integer.MAX_VALUE) < distanceToStart){
            nextPositions.add(tn);
            break;
         }
      }

      return nextPositions;
   }

   private void backTrackAndFindPossibleTilesOnShortestPaths(Node currentNode, Node startNode, Map<Node, Integer> allNodesEvaluated, char[][] grid, Set<String> pathTile)
   {
      pathTile.add(currentNode.uniqueKey);
      int distanceToStart = allNodesEvaluated.get(currentNode);

      if(currentNode.equals(startNode)){
         // done!
         return;
      }else
      {
         final List<Node> nextPositions = findAndAddBacktrackTiles(currentNode, allNodesEvaluated, grid, distanceToStart);;
         for(final Node nextPosition : nextPositions){
            backTrackAndFindPossibleTilesOnShortestPaths(nextPosition, startNode, allNodesEvaluated, grid, pathTile);
         }
      }
   }


   private static List<Node> getTurningsOfThisNode(Node current, char[][] grid)
   {
      final List<Node> thisNodeTurns = new ArrayList<>();

      switch (current.direction){
         case "N":
         case "S":
            thisNodeTurns.add(new Node(current.x, current.y, "E"));
            thisNodeTurns.add(new Node(current.x, current.y, "W"));
            break;
         case "E":
         case "W":
            thisNodeTurns.add(new Node(current.x, current.y, "N"));
            thisNodeTurns.add(new Node(current.x, current.y, "S"));
            break;
      }
      return thisNodeTurns;
   }

   private static Optional<Node> getDirectNeighborAhead(Node current, char[][] grid)
   {
      switch (current.direction)
      {
         case "N":
            if (isReachable(current.x, current.y - 1, grid))
            {
               return Optional.of(new Node(current.x, current.y - 1, current.direction));
            }
            break;
         case "E":
            if (isReachable(current.x + 1, current.y, grid))
            {
               return Optional.of(new Node(current.x + 1, current.y, current.direction));
            }
            break;
         case "S":
            if (isReachable(current.x, current.y + 1, grid))
            {
               return Optional.of(new Node(current.x, current.y + 1, current.direction));
            }
            break;
         case "W":
            if (isReachable(current.x - 1, current.y, grid))
            {
               return Optional.of(new Node(current.x - 1, current.y, current.direction));
            }
            break;
         default:
            return Optional.empty();

      }
      return Optional.empty();
   }

   private static Optional<Node> getDirectNeighborBehind(Node current, char[][] grid)
   {
      switch (current.direction)
      {
         case "N":
            if (isReachable(current.x, current.y + 1, grid))
            {
               return Optional.of(new Node(current.x, current.y + 1, current.direction));
            }
            break;
         case "E":
            if (isReachable(current.x - 1, current.y, grid))
            {
               return Optional.of(new Node(current.x - 1, current.y, current.direction));
            }
            break;
         case "S":
            if (isReachable(current.x, current.y - 1, grid))
            {
               return Optional.of(new Node(current.x, current.y - 1, current.direction));
            }
            break;
         case "W":
            if (isReachable(current.x + 1, current.y, grid))
            {
               return Optional.of(new Node(current.x + 1, current.y, current.direction));
            }
            break;
         default:
            return Optional.empty();
      }
      return Optional.empty();
   }

   private List<Node> getPossibleEndNodes(char[][] grid)
   {
      List<Node> possibleEndNodes = new ArrayList<>();

      for(String direction : directions) {
         Node pEnd = getCharacterPosition('E', grid, direction).get();
         possibleEndNodes.add(pEnd);
      }

      return possibleEndNodes;
   }

   private Optional<Node> getCharacterPosition(final char c, final char[][] grid, String direction)
   {
      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[y].length; x++) {
            if (grid[y][x] == c) {
               return Optional.of(new Node(x, y, direction));
            }
         }
      }
      return Optional.empty();
   }

   private char[][] makeTheGrid()
   {
      char[][] grid = new char[input.size()][];
      for (int i = 0; i < input.size(); i++) {
         grid[i] = input.get(i).toCharArray();
      }
      return grid;
   }


   private static boolean isReachable(int newX, int newY, char[][] grid)
   {
      int rows = grid.length;
      int cols = grid[0].length;

      return isInBounds(newX, newY, rows, cols) && grid[newY][newX] != '#';
   }

   private static boolean isInBounds(int newX, int newY, int rows, int cols)
   {
      return newX >= 0 && newY >= 0 && newX < cols && newY < rows;
   }

   static class Node implements Comparable<Node> {
      int x, y;
      String uniqueKey; //"x-y"
      String direction; // "N", "E", "S", "W"

      Node(int x, int y, String direction) {
         this.x = x;
         this.y = y;
         this.direction = direction;
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
         return Objects.hash(x, y, direction);
      }
   }
}

