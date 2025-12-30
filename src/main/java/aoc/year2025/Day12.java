package aoc.year2025;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
   * this was tough too.
   Stuff to learn:
   * Bitsets: https://www.baeldung.com/java-bitset
   * More dfs...
 */

public class Day12
{
   private final List<String> input;

   // Parsed data
   private final Map<Integer, Shape> shapesByIndex = new HashMap<>();
   private final List<Region> regions = new ArrayList<>();

   // For each shape index: list of unique orientations (as normalized positions)
   private final Map<Integer, List<List<Position>>> orientationsByShape = new HashMap<>();



   public Day12(List<String> input) {

      this.input = input;
      parseTheInput(input);
      precomputeAllOrientations();
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }


      long solvePartOne = solvePartOne();
      System.out.println("Part 1: " + solvePartOne);
      System.out.println("Thanks for this year AoC! See you next December!");
   }

   // =========================
   // Part One: How many of the regions can fit all the presents listed?
   // =========================

   private long solvePartOne() {
      long count = 0;
      for (Region r : regions) {
         if (canFitAllPresents(r)) {
            count++;
         }
      }
      return count;
   }

   /**
    * Determines whether a single region (W x H) can accommodate all required presents.
    *
    * This method performs three phases:
    *
    *  (A) Expand counts -> concrete list of required items
    *      The input provides counts per shape index (e.g., shape 4 x 3, shape 5 x 2).
    *      We expand that into a list like [4,4,4,5,5]. Each element represents ONE
    *      present instance that must be placed (no stacking, no overlap).
    *
    *  (B) Precompute all legal placements for each shape index needed in this region
    *      Using the precomputed unique orientations (rotations + flips), we enumerate
    *      every translation of each orientation that fits entirely within the region.
    *      Each translation becomes a "placement", represented as an occupancy mask
    *      (BitSet) over the region cells.
    *
    *      After this step, geometry is finished; the solver only combines masks.
    *
    *  (C) Backtracking search (DFS) over placements
    *      We build one ShapeVariation per required present instance, each carrying
    *      the same placement list for its shape index.
    *
    *      Key pruning heuristic: sort required instances by number of placements.
    *      This is "most constrained first" and usually reduces the branching factor
    *      significantly (fail fast).
    *
    * Necessary early failure:
    *  - If total required '#' area > region area, packing is impossible.
    *    (This is necessary but not sufficient: holes / geometry can still prevent packing.)
    *
    * Returns:
    *  - true  if a non-overlapping set of placements exists that places every present
    *  - false otherwise
    */
   private boolean canFitAllPresents(Region region) {
      int regionArea = region.width * region.length;

      // Expand counts into a list of required shape indices
      List<Integer> required = new ArrayList<>();
      int totalPresentArea = 0;


      for (int shapeIndex = 0; shapeIndex < region.shapeCounts.size(); shapeIndex++) {
         int c = region.shapeCounts.get(shapeIndex);
         if (c <= 0) continue;

         Shape s = shapesByIndex.get(shapeIndex);
         if (s == null) {
            throw new IllegalStateException("Region references unknown shape index: " + shapeIndex);
         }
         int area = s.positions.size();
         totalPresentArea += area * c;

         for (int k = 0; k < c; k++)
            required.add(shapeIndex);
      }

      // Necessary condition
      if (totalPresentArea > regionArea)
         return false;

      // For each distinct shape index in this region, generate all legal placements in this region
      Map<Integer, List<BitSet>> placementsByShape = new HashMap<>();
      Map<Integer, Integer> areaByShape = new HashMap<>();

      for (int shapeIndex : new HashSet<>(required)) {
         List<List<Position>> orientations = orientationsByShape.get(shapeIndex);
         if (orientations == null || orientations.isEmpty()) {
            throw new IllegalStateException("No orientations for shape index: " + shapeIndex);
         }

         Shape shape = shapesByIndex.get(shapeIndex);
         areaByShape.put(shapeIndex, shape.positions.size());

         List<BitSet> placements = generatePlacements(region.width, region.length, orientations);

         if (placements.isEmpty()) {
            // This shape cannot be placed anywhere at all, therefore region fails
            return false;
         }
         placementsByShape.put(shapeIndex, placements);
      }

      // Build list of required "shape variations" (each required present is one ShapeVariation)
      List<ShapeVariation> variations = new ArrayList<>(required.size());
      for (int shapeIndex : required) {
         variations.add(new ShapeVariation(shapeIndex, areaByShape.get(shapeIndex), placementsByShape.get(shapeIndex)));
      }

      // Heuristic: place most constrained first (fewest placements)
      variations.sort(Comparator.comparingInt(v -> v.placements.size()));

      // DFS backtracking with occupancy bitset
      BitSet occupied = new BitSet(regionArea);
      return dfsPlace(0, variations, occupied);
   }

   /**
    * Depth-first search that tries to place all required present instances.
    *
    * State:
    *  - i: index of the next ShapeVariation (present instance) to place
    *  - variations: ordered list of required instances (usually sorted by fewest placements first)
    *  - occupied: BitSet marking which region cells are already filled by previously placed presents
    *
    * Transition:
    *  - Choose a placement mask for variations[i]
    *  - If it does not overlap occupied (no shared '#'), we apply it:
    *        occupied = occupied OR placement
    *    Recurse to i+1
    *    If recursion fails, undo:
    *        occupied = occupied AND NOT placement
    *
    * Termination:
    *  - If i == variations.size(), all instances have been placed successfully -> return true.
    *
    * Correctness:
    *  - Because we try every placement option for each required instance, this explores the
    *    entire feasible search space (with pruning only from overlap checks and ordering).
    *
    * Performance note:
    *  - The overlap test and apply/undo operations are the hot path; BitSet operations are
    *    used to make them word-level and fast.
    */
   private boolean dfsPlace(int i, List<ShapeVariation> variations, BitSet occupied) {
      if (i == variations.size()) return true;

      ShapeVariation v = variations.get(i);

      for (BitSet placement : v.placements) {
         // If this placement conflicts with already-occupied cells, skip it.
         if (!placement.intersects(occupied)) {
            // Apply placement
            occupied.or(placement);

            // Recurse to place the next present instance
            if (dfsPlace(i + 1, variations, occupied))
               return true;

            // Undo placement (backtrack)
            occupied.andNot(placement);
         }
      }
      return false;
   }

   private List<BitSet> generatePlacements(int W, int H, List<List<Position>> orientations) {
      // For each orientation, slide it across the region grid and record
      // every position where it fits entirely within bounds.
      // Each placement is precomputed so the DFS only performs overlap checks.

      List<BitSet> placements = new ArrayList<>();
      int area = W * H;

      for (List<Position> ori : orientations) {
         // Determine bounding box of this orientation
         int maxX = 0, maxY = 0;
         for (Position p : ori) {
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
         }
         int oriW = maxX + 1;
         int oriH = maxY + 1;

         // Slide across all valid translations
         for (int ty = 0; ty <= H - oriH; ty++) {
            for (int tx = 0; tx <= W - oriW; tx++) {
               BitSet bs = new BitSet(area);
               for (Position p : ori) {
                  int x = tx + p.x;
                  int y = ty + p.y;
                  int idx = y * W + x;
                  bs.set(idx);
               }
               placements.add(bs);
            }
         }
      }

      // ============================================================
      // OPTIONAL DEDUPLICATION
      // ============================================================
      boolean DEDUPE_PLACEMENTS = true; // set to false to skip dedupe

      if (!DEDUPE_PLACEMENTS) {
         return placements;
      }

      HashSet<LongArrayKey> seen = new HashSet<>();
      List<BitSet> unique = new ArrayList<>(placements.size());
      for (BitSet b : placements) {
         long[] arr = b.toLongArray();
         LongArrayKey key = new LongArrayKey(arr);
         if (seen.add(key)) unique.add(b);
      }
      return unique;
   }

   // =========================
   // Precompute & Parsing
   // =========================

   /*
    * =========================
    * Precomputation overview
    * =========================
    *
    * This solver treats the problem as an exact 2D packing / tiling problem.
    * The expensive parts of that problem are:
    *
    *   1) Enumerating all rotations and reflections of each shape
    *   2) Enumerating all legal placements of those shape variations inside a region
    *
    * Both of these steps are independent of the actual search order and are therefore
    * precomputed once so the backtracking search itself can be as fast as possible.
    *
    * ------------------------------------------------------------
    * 1) Shape orientation precomputation
    * ------------------------------------------------------------
    * For each input shape, we generate all *unique* orientations:
    *
    *   - 4 rotations (0°, 90°, 180°, 270°)
    *   - × 2 mirror states (unflipped + flipped)
    *
    * This yields up to 8 orientations per shape, but fewer if the shape is symmetric.
    *
    * Each orientation is:
    *   - Transformed (rotated / flipped)
    *   - Normalized so its minimum (x,y) becomes (0,0)
    *   - Sorted and deduplicated using a canonical signature
    *
    * The result is stored in:
    *
    *   Map<shapeIndex, List<orientation>>
    *
    * where each orientation is a list of occupied (x,y) positions.
    *
    * This ensures:
    *   - No duplicate orientations
    *   - No repeated rotation logic during the search
    *
    * ------------------------------------------------------------
    * 2) Placement precomputation (per region)
    * ------------------------------------------------------------
    * For a given region size (W x H) and a given shape orientation, we slide the
    * shape across the grid and generate all legal placements where:
    *
    *   - The shape remains fully inside the region
    *   - All '#' cells land exactly on grid coordinates
    *
    * Each placement is encoded as a compact occupancy representation
    * (e.g., BitSet or int[] of cell indices).
    *
    * This converts geometric reasoning into fast set operations:
    *
    *   - Overlap checks become a single intersects() call (or small loop)
    *   - Apply / undo operations are constant-time bitwise operations
    *
    * ------------------------------------------------------------
    * 3) Impact on the search
    * ------------------------------------------------------------
    * After precomputation:
    *
    *   - The backtracking search only chooses between precomputed placements
    *   - No geometry, rotation, or bounds checks occur in the DFS
    *   - The search is limited to pure combinatorial branching
    *
    * This dramatically reduces the constant factors in an otherwise exponential
    * search and is essential for performance on larger inputs.
    */

   private void precomputeAllOrientations() {
      for (Shape shape : shapesByIndex.values()) {
         orientationsByShape.put(shape.index, computeUniqueOrientations(shape.positions));
      }
   }

   /**
    * Compute all unique orientations (rotations 0/90/180/270) x (flip or not).
    * Each orientation is normalized so its min x,y becomes (0,0) and positions are sorted.
    */
   private List<List<Position>> computeUniqueOrientations(List<Position> original) {
      List<List<Position>> result = new ArrayList<>();
      Set<String> seen = new HashSet<>();

      // Convert to list for transformations
      List<Position> base = new ArrayList<>(original);

      for (int flip = 0; flip < 2; flip++) {

         List<Position> current = (flip == 0) ? base : flipX(base);
         for (int rot = 0; rot < 4; rot++) {
            List<Position> rotated = (rot == 0) ? current : rotate90(current);
            List<Position> norm = normalize(rotated);

            String sig = signature(norm);
            if (seen.add(sig)) {
               result.add(norm);
            }
            current = rotated;
         }
      }

      return result;
   }

   private List<Position> flipX(List<Position> pts) {
      // Mirror across Y axis: x -> -x
      List<Position> out = new ArrayList<>(pts.size());
      for (Position p : pts) out.add(new Position(-p.x, p.y));
      return out;
   }

   private List<Position> rotate90(List<Position> pts) {
      // 90 degrees clockwise: (x,y) -> (y, -x)
      List<Position> out = new ArrayList<>(pts.size());
      for (Position p : pts) out.add(new Position(p.y, -p.x));
      return out;
   }

   private List<Position> normalize(List<Position> pts) {
      int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
      for (Position p : pts) {
         minX = Math.min(minX, p.x);
         minY = Math.min(minY, p.y);
      }
      List<Position> out = new ArrayList<>(pts.size());
      for (Position p : pts) out.add(new Position(p.x - minX, p.y - minY));

      out.sort(Comparator.<Position>comparingInt(a -> a.y).thenComparingInt(a -> a.x));
      return out;
   }

   private String signature(List<Position> pts) {
      StringBuilder sb = new StringBuilder();
      for (Position p : pts) {
         sb.append(p.x).append(',').append(p.y).append(';');
      }
      return sb.toString();
   }

   private void parseTheInput(List<String> input) {
      shapesByIndex.clear();
      regions.clear();

      int i = 0;

      // Parse shapes until we hit a line that looks like "WxH:"
      Pattern shapeHeader = Pattern.compile("^(\\d+):\\s*$");
      Pattern regionHeader = Pattern.compile("^(\\d+)x(\\d+):\\s*(.*)$");

      while (i < input.size()) {
         String line = input.get(i).trim();
         if (line.isEmpty()) { i++; continue; }

         Matcher rm = regionHeader.matcher(line);
         if (rm.matches()) break; // start of regions section

         Matcher sm = shapeHeader.matcher(line);
         if (!sm.matches()) {
            throw new IllegalArgumentException("Unexpected line while parsing shapes: '" + input.get(i) + "'");
         }

         int index = Integer.parseInt(sm.group(1));
         i++;

         List<String> grid = new ArrayList<>();
         while (i < input.size()) {
            String row = input.get(i);
            if (row.trim().isEmpty()) break;
            // Stop if we accidentally reached regions without blank line (defensive)
            if (regionHeader.matcher(row.trim()).matches()) break;
            grid.add(row.trim());
            i++;
         }

         if (grid.isEmpty()) {
            throw new IllegalArgumentException("Shape " + index + " has no grid rows.");
         }

         List<Position> positions = new ArrayList<>();
         for (int y = 0; y < grid.size(); y++) {
            String row = grid.get(y);
            for (int x = 0; x < row.length(); x++) {
               if (row.charAt(x) == '#') {
                  positions.add(new Position(x, y));
               }
            }
         }

         if (positions.isEmpty()) {
            throw new IllegalArgumentException("Shape " + index + " has no # cells.");
         }

         // Normalize stored shape positions too (not required, but makes equality/hash consistent)
         positions = normalize(positions);
         shapesByIndex.put(index, new Shape(index, positions));

         // Skip blank lines between shapes
         while (i < input.size() && input.get(i).trim().isEmpty()) i++;
      }

      // Parse regions
      while (i < input.size()) {
         String line = input.get(i).trim();
         i++;
         if (line.isEmpty()) continue;

         Matcher rm = regionHeader.matcher(line);
         if (!rm.matches()) {
            throw new IllegalArgumentException("Unexpected line while parsing regions: '" + line + "'");
         }

         int width = Integer.parseInt(rm.group(1));
         int length = Integer.parseInt(rm.group(2));
         String countsPart = rm.group(3).trim();

         List<Integer> counts = new ArrayList<>();
         if (!countsPart.isEmpty()) {
            String[] parts = countsPart.split("\\s+");
            for (String p : parts) counts.add(Integer.parseInt(p));
         }

         // Regions list quantities for each shape index in order (0..N-1). If shapes are sparse or
         // the list is shorter than max shape index + 1, pad with zeros defensively.
         int maxIndex = shapesByIndex.keySet().stream().mapToInt(v -> v).max().orElse(-1);
         int needSize = maxIndex + 1;
         while (counts.size() < needSize) counts.add(0);

         regions.add(new Region(width, length, counts));
      }
   }

   // =========================
   // Classes
   // =========================

   static class Position
   {
      int x,y;

      public Position(int x, int y)
      {
         this.x = x;
         this.y = y;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
            return true;
         if (!(o instanceof Position position))
            return false;
         return x == position.x && y == position.y;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(x, y);
      }

      @Override
      public String toString()
      {
         return "Position{" + "x=" + x + ", y=" + y + '}';
      }
   }

   static class Shape {
      int index;
      List<Position> positions;

      public Shape(int index, List<Position> positions)
      {
         this.index = index;
         this.positions = positions;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
            return true;
         if (!(o instanceof Shape shape))
            return false;
         return Objects.equals(positions, shape.positions);
      }

      @Override
      public int hashCode()
      {
         return Objects.hashCode(positions);
      }
   }

   static class Region
   {
      int width,length;
      // counts per shape index
      List<Integer> shapeCounts;

      public Region(int width, int length, List<Integer> shapeCounts)
      {
         this.width = width;
         this.length = length;
         this.shapeCounts = shapeCounts;
      }

      @Override
      public boolean equals(Object o)
      {
         if (this == o)
            return true;
         if (!(o instanceof Region region))
            return false;
         return width == region.width && length == region.length && Objects.equals(shapeCounts, region.shapeCounts);
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(width, length, shapeCounts);
      }
   }

   /**
    * One required present instance to place in the region:
    * - shapeIndex: which base shape it is
    * - placements: all legal placements of that shape in this region (bitmasks)
    */
   static class ShapeVariation {
      final int shapeIndex;
      final int area;
      final List<BitSet> placements;

      ShapeVariation(int shapeIndex, int area, List<BitSet> placements) {
         this.shapeIndex = shapeIndex;
         this.area = area;
         this.placements = placements;
      }
   }

   /**
    * OPTIONAL helper used only if placement deduplication is enabled.
    * Wraps BitSet.toLongArray() so it can be used as a HashSet key.
    */
   static class LongArrayKey {
      final long[] data;
      final int hash;

      LongArrayKey(long[] data) {
         this.data = data;
         this.hash = Arrays.hashCode(data);
      }

      @Override public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof LongArrayKey other)) return false;
         return Arrays.equals(this.data, other.data);
      }

      @Override public int hashCode() {
         return hash;
      }
   }
}