package aoc.year2025;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;


public class Day09
{
   private final List<String> input;

   //List<Position> grid = new ArrayList<>();
   List<Position> redTiles = new ArrayList<>();

   public Day09(List<String> input) {

      this.input = input;
      parseTheGrid(input);
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      long solvePartOne = findLargestRectangleAreaPart1();
      System.out.println("Part 1: " + solvePartOne);
      long solvePartTwo = findLargestRectangleAreaPart2();
      System.out.println("Part 2: " + solvePartTwo);
   }

   private long findLargestRectangleAreaPart1() {
      if (redTiles.size() < 2) return 0;

      long best = 0;

      for (int i = 0; i < redTiles.size(); i++) {
         Position a = redTiles.get(i);

         for (int j = i + 1; j < redTiles.size(); j++) {
            Position b = redTiles.get(j);

            int dx = Math.abs(a.x - b.x);
            int dy = Math.abs(a.y - b.y);

            long area = (long) (dx + 1) * (dy + 1);
            if (area > best) best = area;
         }
      }

      // If the puzzle guarantees it fits in int, this is safe:
      return best;
   }

   private long findLargestRectangleAreaPart2() {
      if (redTiles.size() < 2) return 0;

      // 1) Build compressed axes (include v and v+1 so unit-thick boundary strips exist)
      int[] xs = buildAxis(redTiles, true);
      int[] ys = buildAxis(redTiles, false);

      int nx = xs.length;
      int ny = ys.length;

      // Cells are between coordinates: cellX in [0..nx-2], cellY in [0..ny-2]
      int cxN = nx - 1;
      int cyN = ny - 1;

      // 2) Mark boundary tiles as filled cells
      boolean[][] filled = buildFilledBooleanGrid(cxN, cyN, ys, xs);

      // 3) Flood fill "outside" over unfilled cells
      boolean[][] outside = buildOutsideBooleanGrid(cxN, cyN, filled);

      // 4) Build weighted prefix sums of allowed tiles:
      // allowed = filled (boundary tiles) OR interior (not outside)
      long[][] pref = buildPrefixSumsOfAllowedTiles(cxN, cyN, xs, ys, filled, outside);

      // 5) Try all red pairs; rectangle validity in O(1) via prefix sum
      long best = 0;

      for (int i = 0; i < redTiles.size(); i++) {
         Position a = redTiles.get(i);
         for (int j = i + 1; j < redTiles.size(); j++) {
            Position b = redTiles.get(j);

            int x1 = Math.min(a.x, b.x);
            int x2 = Math.max(a.x, b.x);
            int y1 = Math.min(a.y, b.y);
            int y2 = Math.max(a.y, b.y);

            // Rectangle in tile coordinates is [x1..x2] x [y1..y2]
            // Convert to half-open: [x1, x2+1) x [y1, y2+1)
            int cx1 = lowerBound(xs, x1);
            int cx2 = lowerBound(xs, x2 + 1);
            int cy1 = lowerBound(ys, y1);
            int cy2 = lowerBound(ys, y2 + 1);

            long allowedTiles =
                  pref[cx2][cy2]
                        - pref[cx1][cy2]
                        - pref[cx2][cy1]
                        + pref[cx1][cy1];

            long area = (long) (x2 - x1 + 1) * (long) (y2 - y1 + 1);

            if (allowedTiles == area && area > best) {
               best = area;
            }
         }
      }

      return best;
   }

   private static long[][] buildPrefixSumsOfAllowedTiles(int cxN, int cyN, int[] xs, int[] ys, boolean[][] filled, boolean[][] outside)
   {
      long[][] pref = new long[cxN + 1][cyN + 1];

      for (int cx = 0; cx < cxN; cx++) {
         long wX = (long) xs[cx + 1] - xs[cx]; // tile-width represented by this cell column
         for (int cy = 0; cy < cyN; cy++) {
            long wY = (long) ys[cy + 1] - ys[cy]; // tile-height represented by this cell row
            boolean allowed = filled[cx][cy] || !outside[cx][cy];
            long val = allowed ? (wX * wY) : 0L;
            pref[cx + 1][cy + 1] = val + pref[cx][cy + 1] + pref[cx + 1][cy] - pref[cx][cy];
         }
      }
      return pref;
   }

   private static boolean[][] buildOutsideBooleanGrid(int cxN, int cyN, boolean[][] filled)
   {
      boolean[][] outside = new boolean[cxN][cyN];
      ArrayDeque<int[]> q = new ArrayDeque<>();

      // Start from a guaranteed outside cell: pick cell (0,0) which corresponds to minX.. and minY..
      // Because we added margins in buildAxis, this is outside the loop.
      outside[0][0] = true;
      q.add(new int[]{0, 0});

      final int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
      while (!q.isEmpty()) {
         int[] cur = q.removeFirst();
         int x = cur[0], y = cur[1];

         for (int[] d : dirs) {
            int nxC = x + d[0], nyC = y + d[1];
            if (nxC < 0 || nxC >= cxN || nyC < 0 || nyC >= cyN) continue;
            if (outside[nxC][nyC]) continue;
            if (filled[nxC][nyC]) continue; // boundary blocks traversal
            outside[nxC][nyC] = true;
            q.addLast(new int[]{nxC, nyC});
         }
      }
      return outside;
   }

   private boolean[][] buildFilledBooleanGrid(int cxN, int cyN, int[] ys, int[] xs)
   {
      boolean[][] filled = new boolean[cxN][cyN];

      for (int i = 0; i < redTiles.size(); i++) {
         Position a = redTiles.get(i);
         Position b = redTiles.get((i + 1) % redTiles.size());

         if (a.y == b.y) {
            // horizontal segment on row y, spanning x from min..max inclusive tiles
            int y = a.y;
            int x1 = Math.min(a.x, b.x);
            int x2 = Math.max(a.x, b.x);

            int cy = lowerBound(ys, y);          // interval [y, y+1)
            int cx1 = lowerBound(xs, x1);
            int cx2 = lowerBound(xs, x2 + 1);    // exclusive end

            // fill strip cells along that row
            for (int cx = cx1; cx < cx2; cx++) {
               filled[cx][cy] = true;
            }

         } else if (a.x == b.x) {
            // vertical segment on column x, spanning y from min..max inclusive tiles
            int x = a.x;
            int y1 = Math.min(a.y, b.y);
            int y2 = Math.max(a.y, b.y);

            int cx = lowerBound(xs, x);          // interval [x, x+1)
            int cy1 = lowerBound(ys, y1);
            int cy2 = lowerBound(ys, y2 + 1);    // exclusive end

            for (int cy = cy1; cy < cy2; cy++) {
               filled[cx][cy] = true;
            }

         } else {
            throw new IllegalArgumentException("Adjacent red tiles must share a row or column.");
         }
      }
      return filled;
   }

   private static int[] buildAxis(List<Position> redTiles, boolean isX)
   {
      TreeSet<Integer> set = new TreeSet<>();
      int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

      for (Position p : redTiles) {
         int v = isX ? p.x : p.y;
         min = Math.min(min, v);
         max = Math.max(max, v);

         // Ensure unit-thick strips exist at the boundary coordinate
         set.add(v);
         set.add(v + 1);
      }

      // Add a safety margin so (0,0) cell is guaranteed outside
      set.add(min - 1);
      set.add(min);
      set.add(max + 1);
      set.add(max + 2);

      int[] axis = new int[set.size()];
      int i = 0;
      for (int v : set) axis[i++] = v;
      return axis;
   }

   private static int lowerBound(int[] a, int v) {
      int lo = 0, hi = a.length;
      while (lo < hi) {
         int mid = (lo + hi) >>> 1;
         if (a[mid] < v) lo = mid + 1;
         else hi = mid;
      }
      return lo;
   }
   private void parseTheGrid(List<String> input) {
      redTiles.clear();

      for (String line : input) {
         if (line == null || line.isBlank()) continue;
         String[] parts = line.trim().split(",");
         int x = Integer.parseInt(parts[0]);
         int y = Integer.parseInt(parts[1]);
         redTiles.add(new Position(x, y));
      }
   }


   static class Position {
      final int x;
      final int y;

      public Position(int x, int y)
      {
         this.x = x;
         this.y = y;
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

   }
}

