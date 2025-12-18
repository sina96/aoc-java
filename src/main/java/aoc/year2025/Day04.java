package aoc.year2025;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class Day04
{
   private final List<String> input;

   List<Position> grid = new ArrayList<>();

   public Day04(List<String> input) {
      this.input = input;
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      grid = makeTheGrid(input);
      Set<Position> paperPositionSet = grid.stream()
            .filter(Position::hasPaperRoll)
            .collect(Collectors.toSet());

      int solvePartOne = countAccessiblePaperPositions(paperPositionSet);
      System.out.println("Part 1: " + solvePartOne);

      int solvePartTwo = countLoopedAccessiblePaperPositions(paperPositionSet);
      System.out.println("Part 2: " + solvePartTwo);
   }

   private int countAccessiblePaperPositions(Set<Position> paperPositionSet)
   {
      List<Position> accessiblePaperPositions = accessPaperRolls(paperPositionSet);
      return accessiblePaperPositions.size();
   }

   private int countLoopedAccessiblePaperPositions(Set<Position> paperPositionSet)
   {
      int sum = 0;

      List<Position> accessiblePaperPositions = accessPaperRolls(paperPositionSet);

      if (!accessiblePaperPositions.isEmpty()) {
         sum += accessiblePaperPositions.size();
         accessiblePaperPositions.forEach(paperPositionSet::remove);
         sum += countLoopedAccessiblePaperPositions(paperPositionSet);
      }
      return sum;
   }


   private static List<Position> accessPaperRolls(Set<Position> paperPositionSet)
   {
      List<Position> accessiblePaperPositions = new ArrayList<>();

      for (Position position : paperPositionSet)
      {
         int count = checkAdjacentPositions(paperPositionSet, position);

         if (count < 4)
            accessiblePaperPositions.add(position);
      }
      return accessiblePaperPositions;
   }

   private static int checkAdjacentPositions(Set<Position> paperPositionSet, Position position)
   {
      int count = 0;
      if(paperPositionSet.contains(new Position(position.getX(), position.getY() - 1)))
         count++;
      if(paperPositionSet.contains(new Position(position.getX(), position.getY() + 1)))
         count++;
      if (paperPositionSet.contains(new Position(position.getX() + 1, position.getY())))
         count++;
      if(paperPositionSet.contains(new Position(position.getX() - 1, position.getY())))
         count++;
      if(paperPositionSet.contains(new Position(position.getX() + 1, position.getY() + 1)))
         count++;
      if (paperPositionSet.contains(new Position(position.getX() - 1, position.getY() - 1)))
         count++;
      if (paperPositionSet.contains(new Position(position.getX() - 1, position.getY() + 1)))
         count++;
      if (paperPositionSet.contains(new Position(position.getX() + 1, position.getY() - 1)))
         count++;
      return count;
   }

   private List<Position> makeTheGrid(List<String> input)
   {
      List<Position> positions = new ArrayList<>();
      for (int y = 0; y < input.size(); y++) {
         final String line = input.get(y);
         for (int x = 0; x < line.length(); x++) {
            boolean hasPaper = line.charAt(x) == '@';
            positions.add(new Position(x, y, hasPaper));
         }
      }
      return positions;
   }

   static class Position {
      final int x;
      final int y;
      boolean withPaperRoll;

      public Position(int x, int y)
      {
         this.x = x;
         this.y = y;
         this.withPaperRoll = false;
      }

      public Position(int x, int y, boolean withPaperRoll)
      {
         this.x = x;
         this.y = y;
         this.withPaperRoll = withPaperRoll;
      }

      @Override
      public boolean equals(Object obj)
      {
         return (obj instanceof Position) && ((Position) obj).x == x && ((Position) obj).y == y;
      }

      @Override
      public int hashCode() {
         return Objects.hash(x, y);
      }

      public int getX()
      {
         return x;
      }

      public int getY()
      {
         return y;
      }

      public boolean hasPaperRoll()
      {
         return withPaperRoll;
      }
   }
}

