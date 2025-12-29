package aoc.year2025;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* this was tough, need to revise */

public class Day10
{
   private final List<String> input;

   private List<Machine> machines = new ArrayList<>();

   public Day10(List<String> input) {

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
   // Part One
   // =========================


   private long solvePartOne() {
      long total = 0;
      for (Machine m : machines) {
         total += minButtonPresses(m);
      }
      return total;
   }

   private int minButtonPresses(Machine m) {
      // BFS

      int start = 0;
      int target = m.targetMask;

      Queue<Integer> queue = new ArrayDeque<>();
      Map<Integer, Integer> dist = new HashMap<>();

      queue.add(start);
      dist.put(start, 0);

      while (!queue.isEmpty()) {
         int current = queue.poll();
         int steps = dist.get(current);

         if (current == target) {
            return steps;
         }

         for (int button : m.buttonMasks) {
            int next = current ^ button;
            if (!dist.containsKey(next)) {
               dist.put(next, steps + 1);
               queue.add(next);
            }
         }
      }

      throw new IllegalStateException("No solution found");
   }

   // =========================
   // Part Two
   // =========================

   private long solvePartTwo() {
      long total = 0;
      for (Machine m : machines) {
         total += minPressesPartTwo(m);
      }
      return total;
   }

   private long minPressesPartTwo(Machine m) {
      return new PartTwoSolver(m.joltageTargets, m.buttonIndices).solve();
   }

   private void parseTheInput(List<String> input) {
      machines = new ArrayList<>();

      Pattern lightPattern = Pattern.compile("\\[([^]]*)\\]");
      Pattern buttonPattern = Pattern.compile("\\(([^)]*)\\)");
      Pattern joltagePattern = Pattern.compile("\\{([^}]*)\\}");

      for (String line : input) {
         Machine m = new Machine();

         // ----- parse lights -----
         Matcher lm = lightPattern.matcher(line);
         if (!lm.find()) throw new IllegalArgumentException(line);

         String lights = lm.group(1).trim();
         m.lightCount = lights.length();
         m.targetMask = 0;
         for (int i = 0; i < lights.length(); i++) {
            if (lights.charAt(i) == '#') {
               m.targetMask |= (1 << i);
            }
         }

         m.buttonMasks = new ArrayList<>();
         m.buttonIndices = new ArrayList<>();

         // ----- parse buttons -----
         Matcher bm = buttonPattern.matcher(line);
         while (bm.find()) {
            String inside = bm.group(1).trim();
            if (inside.isEmpty()) continue;

            String[] parts = inside.split(",");
            int mask = 0;
            int[] idx = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
               int v = Integer.parseInt(parts[i].trim());
               idx[i] = v;
               mask |= (1 << v);
            }

            m.buttonMasks.add(mask);
            m.buttonIndices.add(idx);
         }

         // ----- parse joltage targets -----
         Matcher jm = joltagePattern.matcher(line);
         if (!jm.find()) throw new IllegalArgumentException(line);

         String[] parts = jm.group(1).trim().split(",");
         m.joltageTargets = new int[parts.length];
         for (int i = 0; i < parts.length; i++) {
            m.joltageTargets[i] = Integer.parseInt(parts[i].trim());
         }

         machines.add(m);
      }
   }

   static class Machine {
      // ===== Part One =====
      int targetMask;              // bitmask for lights
      List<Integer> buttonMasks;   // bitmask per button
      int lightCount;

      // ===== Part Two =====
      List<int[]> buttonIndices;   // same buttons, but as index lists
      int[] joltageTargets;        // from {...}
   }

   // credit to https://www.reddit.com/r/adventofcode/comments/1pk87hl/2025_day_10_part_2_bifurcate_your_way_to_victory/
   /*
    * Advent of Code 2025 – Day 10, Part Two
    *
    * Solution overview:
    * ------------------
    * Each machine starts with all joltage counters at 0. Pressing a button
    * increases a fixed subset of counters by +1. The goal is to reach the
    * exact target joltage vector with the fewest total button presses.
    *
    * Key observation:
    * ----------------
    * Any valid sequence of button presses can be reordered into two phases:
    *
    *   Phase 1: Press each button at most once (determines the parity).
    *   Phase 2: Press some sequence of buttons twice (contributes only even amounts).
    *
    * Phase 1 determines the final parity (odd/even) of each counter.
    * Phase 2 is responsible for the remaining even portion.
    *
    * Algorithm:
    * ----------
    * Let f(t) be the minimum number of presses needed to reach target vector t.
    *
    * For every subset S of buttons (there are 2^B total):
    *   - Let inc(S) be the joltage increments caused by pressing each button in S once.
    *   - If t - inc(S) is nonnegative and even in every counter:
    *       * Let t' = (t - inc(S)) / 2
    *       * Then f(t) ≤ |S| + 2 * f(t')
    *
    * The answer f(t) is the minimum value over all valid subsets S.
    *
    * Base case:
    * ----------
    * f(0, 0, ..., 0) = 0
    *
    * Invalid states:
    * ---------------
    * If no subset S produces a nonnegative, even remainder, the target is unreachable.
    *
    * Performance:
    * ------------
    * - Each recursive step reduces all joltage values by roughly half.
    * - The number of counters is small, and B (number of buttons) is small.
    * - Memoization ensures each target vector is solved only once.
    *
    * This approach avoids linear programming, solvers, or large state-space
    * searches, while guaranteeing an exact minimum solution.
    */
   private static final class PartTwoSolver {
      private static final long INF = Long.MAX_VALUE / 4;

      private final int[] target;
      private final List<int[]> buttons;
      private final int K;              // number of counters
      private final int B;              // number of buttons

      // Precomputed for all subsets:
      // inc[subset][i] = how much subset increments counter i in Phase 1
      private final int[][] inc;
      private final int[] pop;

      // memo: key is the current target vector serialized
      private final Map<String, Long> memo = new HashMap<>();

      PartTwoSolver(int[] target, List<int[]> buttons) {
         this.target = target;
         this.buttons = buttons;
         this.K = target.length;
         this.B = buttons.size();

         int subsets = 1 << B;
         this.inc = new int[subsets][K];
         this.pop = new int[subsets];

         precomputeSubsetIncrements();
      }

      long solve() {
         long ans = dfs(target);
         if (ans >= INF) {
            throw new IllegalStateException("No solution for this machine in Part Two.");
         }
         return ans;
      }

      private void precomputeSubsetIncrements() {
         int subsets = 1 << B;

         // Standard DP over subsets:
         // inc[s] = inc[s without lsb] + increment(lsbButton)
         for (int s = 1; s < subsets; s++) {
            int lsb = s & -s;
            int j = Integer.numberOfTrailingZeros(lsb);
            int prev = s ^ lsb;

            pop[s] = pop[prev] + 1;

            // copy previous increments
            System.arraycopy(inc[prev], 0, inc[s], 0, K);

            // add contribution of button j
            for (int idx : buttons.get(j)) {
               inc[s][idx] += 1;
            }
         }
      }

      private long dfs(int[] t) {
         // base case: all zeros
         boolean allZero = true;
         for (int v : t) {
            if (v != 0) { allZero = false; break; }
         }
         if (allZero) return 0;

         String key = keyOf(t);
         Long cached = memo.get(key);
         if (cached != null) return cached;

         long best = INF;
         int subsets = 1 << B;

         // Enumerate all phase-1 subsets
         for (int s = 0; s < subsets; s++) {
            // Compute remainder r = t - inc[s]
            // Must be nonnegative AND even in every coordinate
            boolean ok = true;
            int[] half = new int[K];

            for (int i = 0; i < K; i++) {
               int r = t[i] - inc[s][i];
               if (r < 0) { ok = false; break; }
               if ( (r & 1) != 0 ) { ok = false; break; }
               half[i] = r >>> 1; // r/2
            }
            if (!ok) continue;

            long sub = dfs(half);
            if (sub >= INF) continue;

            long cost = pop[s] + 2L * sub;
            if (cost < best) best = cost;
         }

         memo.put(key, best);
         return best;
      }

      private String keyOf(int[] t) {
         // Fast-enough stable key; K is small (<= ~10)
         // Example: "132,30,23,13,121,115"
         StringBuilder sb = new StringBuilder(K * 4);
         for (int i = 0; i < K; i++) {
            if (i > 0) sb.append(',');
            sb.append(t[i]);
         }
         return sb.toString();
      }
   }
}

