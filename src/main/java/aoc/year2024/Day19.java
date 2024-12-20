package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class Day19
{
   private final List<String> input;

   public Day19(List<String> input)
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
      List<String> availableTowels = List.of(input.get(0).split(",\\s*"));
      List<String> desiredTowels = parseDesiredTowels(input);

      long towelsCanBeMade = desiredTowels.stream()
            .filter(towel -> canBeMade(towel, availableTowels))
            .count();

      System.out.println("Part 1: " + towelsCanBeMade);
   }

   private void solvePartTwo()
   {
      List<String> availableTowels = List.of(input.get(0).split(",\\s*"));
      List<String> desiredTowels = parseDesiredTowels(input);

      long totalNrOfWays = desiredTowels.stream()
            .mapToLong(towel -> countNrWays(towel, availableTowels, new HashMap<>()))
            .sum();

      System.out.println("Part 2: " + totalNrOfWays);
   }

   private List<String> parseDesiredTowels(List<String> input)
   {
      List<String> desiredTowels = new ArrayList<>();
      boolean afterEmptyLine = false;

      for (String line : input) {
         if (line.isEmpty()) {
            afterEmptyLine = true; // Mark when the empty line is found
         } else if (afterEmptyLine) {
            desiredTowels.add(line); // Add lines after the empty line
         }
      }
      return desiredTowels;
   }

   //BFS
   //Each index of the string towel is a node.
   //A substring that matches a towel piece represents an edge connecting one index to another.
   //The goal is to traverse from index 0 to the last index of towel to
   //determine if it's possible to form the string using the pieces in availableTowels
   private boolean canBeMade(String towel, List<String> availableTowels) {
      // Store available towels in a HashSet for O(1) lookup
      Set<String> towelSet = new HashSet<>(availableTowels);

      // Use a queue to process substrings
      Queue<Integer> queue = new LinkedList<>();
      queue.add(0);

      // Keep track of visited indices to avoid redundant work
      boolean[] visited = new boolean[towel.length() + 1];

      while (!queue.isEmpty()) {
         int start = queue.poll();

         // If we've already visited this index, skip it
         if (visited[start]) continue;
         visited[start] = true;

         // Check all possible substrings starting at this index
         for (int end = start + 1; end <= towel.length(); end++) {
            String substring = towel.substring(start, end);

            // If the substring is in the set, add the next index to the queue
            if (towelSet.contains(substring)) {
               queue.add(end);

               // If we've reached the end of the towel, return true
               if (end == towel.length()) {
                  return true;
               }
            }
         }
      }

      // If we exhaust the queue without finding a solution, return false
      return false;
   }


   private long countNrWays(final String towel, List<String> availableTowels, final Map<String, Long> memoMap) {
      // Base case: An empty string can always be constructed in one way
      if (towel.isEmpty()) {
         return 1;
      }

      // Check if the result for this string is already computed
      if (memoMap.containsKey(towel)) {
         return memoMap.get(towel);
      }

      // Convert available towels to a set for fast lookup
      Set<String> towelSet = new HashSet<>(availableTowels);

      // Initialize the count of ways for this substring
      long count = 0;

      // Try all possible prefixes of the current towel
      for (int i = 1; i <= towel.length(); i++) {
         String prefix = towel.substring(0, i);

         // If the prefix matches a towel piece, recursively compute the remaining ways
         if (towelSet.contains(prefix)) {
            count += countNrWays(towel.substring(i), availableTowels, memoMap);
         }
      }

      // Store the computed result in the memoization map
      memoMap.put(towel, count);

      // Return the total count for this substring
      return count;
   }

}

