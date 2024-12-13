package aoc.year2024;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Day12
{
   private final List<String> input;

   public Day12(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      char[][] grid = makeTheGrid(input);

      int totalPriceWithPerimeter = traverseAndCalculateTotalPrice(grid, true);
      int totalPriceWithSide = traverseAndCalculateTotalPrice(grid, false);

      System.out.println("Part 1: " + totalPriceWithPerimeter);
      System.out.println("Part 2: " + totalPriceWithSide);
   }

   private char[][] makeTheGrid(List<String> gridData)
   {
      return gridData.stream().map(String::toCharArray).toArray(char[][]::new);
   }

   private int traverseAndCalculateTotalPrice(final char[][] grid, boolean usePerimeter) {
      final boolean[][] visited = new boolean[grid.length][grid[0].length];
      int totalPrice = 0;
      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[0].length; x++) {
            if (!visited[y][x]) {
               final Set<String> region = traverseAndFindRegion(x, y, grid[y][x], grid, visited);
               if(usePerimeter) {
                  totalPrice += region.size() * calulatePerimeterForRegion(region, grid);
               } else {
                  totalPrice += region.size() * calculateSidesForRegion(region);
               }
            }
         }
      }
      return totalPrice;
   }

   private Set<String> traverseAndFindRegion(int x, int y, char character, char[][] grid, boolean[][] visited)
   {
      final Set<String> result = new HashSet<>();
      visited[y][x] = true;
      result.add(x+"-"+y);
      if (!hasUnvisitedNeighbours(grid,x, y, character, visited)) {
         return result;
      } else {
         if (hasUnvisitedLeftNeighbour(grid,x, y, character, visited)) {
            result.addAll(traverseAndFindRegion(x - 1, y, character, grid, visited));
         }
         if (hasUnvisitedRightNeighbour(grid,x, y, character, visited)) {
            result.addAll(traverseAndFindRegion(x + 1, y, character, grid, visited));
         }
         if (hasUnvisitedUpperNeighbour(grid,x, y, character, visited)) {
            result.addAll(traverseAndFindRegion(x, y - 1, character, grid, visited));
         }
         if (hasUnvisitedLowerNeighbour(grid,x, y, character, visited)) {
            result.addAll(traverseAndFindRegion(x, y + 1, character, grid, visited));
         }
         return result;
      }
   }

   private int calulatePerimeterForRegion(Set<String> region, char[][] grid)
   {
      // Calculating perimeters is just calculating lines
      int total = 0;
      for (final String position : region) {
         int x = Integer.parseInt(position.split("-",2)[0]);
         int y = Integer.parseInt(position.split("-",2)[1]);
         int corners = calculatePerimeter(x, y, grid[y][x], grid);
         total += corners;
      }

      return total;
   }

   private int calculatePerimeter(final int x, final int y, final char character, final char[][] grid) {
      int perimeter = 0;
      if (!hasLeftNeighbour(grid, x, y, character)) {
         perimeter++;
      }
      if (!hasRightNeighbour(grid, x, y, character)) {
         perimeter++;
      }
      if (!hasUpperNeighbour(grid, x, y, character)) {
         perimeter++;
      }
      if (!hasLowerNeighbour(grid, x, y, character)) {
         perimeter++;
      }
      return perimeter;
   }

   private int calculateSidesForRegion(Set<String> region)
   {
      // Calculating sides is just calculating corners
      int total = 0;
      for (final String position : region) {
         int corners = getCornersForPosition(region, position);
         total += corners;
      }

      return total;
   }

   private int getCornersForPosition(Set<String> region, String position)
   {
      int corners = 0;
      if (!hasLeftNeighbour(position, region) && !hasUpperNeighbour(position, region)
            || !hasUpperLeftNeighbour(position, region) && hasLeftNeighbour(position, region)
            && hasUpperNeighbour(position, region)) {
         corners++;
      }
      if (!hasRightNeighbour(position, region) && !hasUpperNeighbour(position, region)
            || !hasUpperRightNeighbour(position, region) && hasRightNeighbour(position, region)
            && hasUpperNeighbour(position, region)) {
         corners++;
      }
      if (!hasRightNeighbour(position, region) && !hasLowerNeighbour(position, region)
            || !hasLowerRightNeighbour(position, region) && hasRightNeighbour(position, region)
            && hasLowerNeighbour(position, region)) {
         corners++;
      }
      if (!hasLeftNeighbour(position, region) && !hasLowerNeighbour(position, region)
            || !hasLowerLeftNeighbour(position, region) && hasLeftNeighbour(position, region)
            && hasLowerNeighbour(position, region)) {
         corners++;
      }
      return corners;
   }

   private boolean hasUnvisitedNeighbours(final char[][] grid, final int x, final int y, final char character, final boolean[][] visited) {
      return hasUnvisitedLeftNeighbour(grid, x, y, character, visited)
            || hasUnvisitedRightNeighbour(grid, x, y, character, visited)
            || hasUnvisitedUpperNeighbour(grid, x, y, character, visited)
            || hasUnvisitedLowerNeighbour(grid, x, y, character, visited);
   }

   private boolean hasLeftNeighbour(final char[][] grid, final int x, final int y, final char character) {
      return isInBounds(grid, x - 1, y) && grid[y][x - 1] == character;
   }

   private boolean hasRightNeighbour(final char[][] grid, final int x, final int y, final char character) {
      return isInBounds(grid, x + 1, y) && grid[y][x + 1] == character;
   }

   private boolean hasUpperNeighbour(final char[][] grid, final int x, final int y, final char character) {
      return isInBounds(grid, x, y - 1) && grid[y - 1][x] == character;
   }

   private boolean hasLowerNeighbour(final char[][] grid, final int x, final int y, final char character) {
      return isInBounds(grid, x, y + 1) && grid[y + 1][x] == character;
   }

   private boolean isInBounds(final char[][] grid, final int x, final int y) {
      return x >= 0 && y >= 0 && y < grid.length && x < grid[y].length;
   }

   private boolean hasUnvisitedLeftNeighbour(final char[][] plot, final int x, final int y, final char name, final boolean[][] visited) {
      return hasLeftNeighbour(plot, x, y, name) && !visited[y][x - 1];
   }

   private boolean hasUnvisitedRightNeighbour(final char[][] plot, final int x, final int y, final char name, final boolean[][] visited) {
      return hasRightNeighbour(plot, x, y, name) && !visited[y][x + 1];
   }

   private boolean hasUnvisitedUpperNeighbour(final char[][] plot, final int x, final int y, final char name, final boolean[][] visited) {
      return hasUpperNeighbour(plot, x, y, name) && !visited[y - 1][x];
   }

   private boolean hasUnvisitedLowerNeighbour(final char[][] grid, final int x, final int y, final char name, final boolean[][] visited) {
      return hasLowerNeighbour(grid, x, y, name) && !visited[y + 1][x];
   }

   private boolean hasUpperLeftNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x - 1 )+ "-"+ (y - 1));
   }

   private boolean hasUpperRightNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x + 1 )+ "-"+ (y - 1));
   }

   private boolean hasLowerRightNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x + 1 )+ "-"+ (y + 1));
   }

   private boolean hasLowerLeftNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x - 1 )+ "-"+ (y + 1));
   }

   private boolean hasLeftNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x - 1 )+ "-"+ (y));
   }

   private boolean hasRightNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x + 1 )+ "-"+ (y));
   }

   private boolean hasUpperNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x)+ "-"+ (y - 1));
   }

   private boolean hasLowerNeighbour(final String position, final Set<String> region) {
      int x = Integer.parseInt(position.split("-",2)[0]);
      int y = Integer.parseInt(position.split("-",2)[1]);
      return region.contains((x)+ "-"+ (y + 1));
   }
}

