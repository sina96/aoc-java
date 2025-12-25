package aoc.year2025;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class Day07 {

   private static final char START = 'S';
   private static final char SPLITTER = '^';

   private final List<String> input;

   // Grid storage
   private List<Position> grid;

   // Fast lookup structures
   private Map<Position, Character> cellByPos;
   private Set<Position> splitters;

   private int width;
   private int height;
   private Position start;

   public Day07(List<String> input) {
      if (input == null || input.isEmpty()) {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      this.input = input;
      parseGrid();
   }

   public void solve() {
      System.out.println("Part 1: " + countSplitsPart1());
      System.out.println("Part 2: " + countTimelinesPart2());
   }

   // ----------------------------
   // Parsing
   // ----------------------------
   private void parseGrid() {
      height = input.size();
      width = input.get(0).length();

      grid = new ArrayList<>(width * height);
      cellByPos = new HashMap<>(width * height * 2);
      splitters = new HashSet<>();

      for (int y = 0; y < height; y++) {
         String line = input.get(y);
         if (line.length() != width) {
            throw new IllegalArgumentException("Non-rectangular input at row " + y);
         }

         for (int x = 0; x < width; x++) {
            char c = line.charAt(x);
            Position p = new Position(x, y);

            grid.add(p);
            cellByPos.put(p, c);

            if (c == START) {
               start = p;
            } else if (c == SPLITTER) {
               splitters.add(p);
            }
         }
      }

      if (start == null) {
         throw new IllegalStateException("No start position 'S' found");
      }
   }

   // ----------------------------
   // Part 1
   // ----------------------------
   private long countSplitsPart1() {
      Set<Position> active = new HashSet<>();
      active.add(start);

      long splits = 0;

      while (!active.isEmpty()) {
         Set<Position> nextActive = new HashSet<>();

         for (Position p : active) {
            int nx = p.getX();
            int ny = p.getY() + 1;

            if (!inBounds(nx, ny)) {
               continue;
            }

            Position next = new Position(nx, ny);
            if (cellByPos.get(next) == SPLITTER) {
               splits++;

               addIfInBounds(nextActive, nx - 1, ny);
               addIfInBounds(nextActive, nx + 1, ny);
            } else {
               nextActive.add(next);
            }
         }

         active = nextActive;
      }

      return splits;
   }

   // ----------------------------
   // Part 2
   // ----------------------------
   private long countTimelinesPart2() {
      int[][] nextSplitterBelow = buildNextSplitterBelow();
      Map<Position, Long> memo = new HashMap<>();
      return countTimelines(start, nextSplitterBelow, memo);
   }

   private int[][] buildNextSplitterBelow() {
      int[][] nextY = new int[width][height + 1];

      for (int x = 0; x < width; x++) {
         int next = -1;
         for (int y = height - 1; y >= 0; y--) {
            nextY[x][y] = next;
            if (cellByPos.get(new Position(x, y)) == SPLITTER) {
               next = y;
            }
         }
         nextY[x][height] = -1;
      }

      return nextY;
   }

   private long countTimelines(Position p, int[][] nextY, Map<Position, Long> memo) {
      if (p.getX() < 0 || p.getX() >= width) return 1L;
      if (p.getY() >= height) return 1L;

      Long cached = memo.get(p);
      if (cached != null) return cached;

      int splitterY = nextY[p.getX()][p.getY()];
      final long result;

      if (splitterY == -1) {
         result = 1L;
      } else {
         long left = countTimelines(new Position(p.getX() - 1, splitterY), nextY, memo);
         long right = countTimelines(new Position(p.getX() + 1, splitterY), nextY, memo);
         result = left + right;
      }

      memo.put(p, result);
      return result;
   }

   // ----------------------------
   // Helpers
   // ----------------------------
   private boolean inBounds(int x, int y) {
      return x >= 0 && x < width && y >= 0 && y < height;
   }

   private void addIfInBounds(Set<Position> set, int x, int y) {
      if (inBounds(x, y)) {
         set.add(new Position(x, y));
      }
   }

   // ----------------------------
   // Position
   // ----------------------------
   static class Position {
      private final int x;
      private final int y;


      Position(int x, int y) {
         this.x = x;
         this.y = y;
      }

      int getX() { return x; }
      int getY() { return y; }

      @Override
      public boolean equals(Object o) {
         return (o instanceof Position other) && other.x == x && other.y == y;
      }

      @Override
      public int hashCode() {
         return Objects.hash(x, y);
      }
   }
}