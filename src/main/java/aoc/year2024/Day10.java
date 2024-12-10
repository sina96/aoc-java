package aoc.year2024;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Day10
{
   private final List<String> input;

   public Day10(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      int[][] grid = makeTheGrid(input);

      int totalScore = traverseAndCalculateTotal(grid, true);
      int totalRating = traverseAndCalculateTotal(grid, false);

      System.out.println("Part 1: " + totalScore);
      System.out.println("Part 2: " + totalRating);
   }

   private int[][] makeTheGrid(List<String> gridData)
   {
      int rows = gridData.size();
      int cols = gridData.get(0).length();
      int[][] gridArray = new int[rows][cols];
      for (int i = 0; i < rows; i++) {
         gridArray[i] = gridData.get(i).chars()
               .map(Character::getNumericValue)
               .toArray();
      }
      return gridArray;
   }

   private int traverseAndCalculateTotal(int[][] grid, boolean useVisited)
   {
      int total = 0;

      for (int row = 0; row < grid.length; row++) {
         for (int col = 0; col < grid[row].length; col++) {
            if (grid[row][col] == 0) {
               if (useVisited) {
                  total += calculateTotalForTrailHead(grid, new Integer[] { row, col }, 0, new HashSet<>());
               } else {
                  total += calculateTotalForTrailHead(grid, new Integer[] { row, col }, 0, null);
               }
            }
         }
      }
      return total;
   }

   private int calculateTotalForTrailHead(int[][] grid, Integer[] currentPosition, int currentValue, Set<String> visited)
   {
      if (visited != null) {
         visited.add(getPosKey(currentPosition));
      }

      if (currentValue == 9) {
         return 1;
      }
      int total = 0;

      Integer[] nextPosition = new Integer[] { currentPosition[0], currentPosition[1] + 1 };
      if (isValidMove(grid, nextPosition, currentValue, visited)) {
         total += calculateTotalForTrailHead(grid, nextPosition, currentValue + 1, visited);
      }

      nextPosition = new Integer[] { currentPosition[0] + 1, currentPosition[1] };
      if (isValidMove(grid, nextPosition, currentValue, visited)) {
         total += calculateTotalForTrailHead(grid, nextPosition, currentValue + 1, visited);
      }

      nextPosition = new Integer[] { currentPosition[0], currentPosition[1] - 1 };
      if (isValidMove(grid, nextPosition, currentValue, visited)) {
         total += calculateTotalForTrailHead(grid, nextPosition, currentValue + 1, visited);
      }

      nextPosition = new Integer[] { currentPosition[0] - 1, currentPosition[1] };
      if (isValidMove(grid, nextPosition, currentValue, visited)) {
         total += calculateTotalForTrailHead(grid, nextPosition, currentValue + 1, visited);
      }

      return total;
   }

   private boolean isValidMove(int[][] grid, Integer[] nextPosition, int currentValue, Set<String> visited)
   {
      return isInBounds(grid, nextPosition)
            && grid[nextPosition[0]][nextPosition[1]] == currentValue + 1
            && (visited == null || !visited.contains(getPosKey(nextPosition)));
   }

   private static boolean isInBounds(int[][] grid, Integer[] position) {
      return position[0] >= 0 && position[0] < grid.length && position[1] >= 0 && position[1] < grid[0].length;
   }

   private static String getPosKey(Integer[] currentPosition)
   {
      return currentPosition[0] + "-" + currentPosition[1];
   }
}

