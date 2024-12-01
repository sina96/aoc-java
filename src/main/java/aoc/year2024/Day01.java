package aoc.year2024;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;


public class Day01 {
   private final List<String> input;
   private final List<Integer> leftNumbers = new ArrayList<>();
   private final List<Integer> rightNumbers = new ArrayList<>();

   public Day01(List<String> input) {
      this.input = input;
   }

   public void solve() {

      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      processInput(input, leftNumbers, rightNumbers);

      int totalDistance = calculateSumOfDistances(leftNumbers, rightNumbers);
      System.out.println("Total Sum of Distances: " + totalDistance);
   }

   private static void processInput(List<String> input, List<Integer> leftNumbers, List<Integer> rightNumbers) {
      input.stream()
            .filter(line -> !line.trim().isEmpty())  // Filter out empty or whitespace-only lines
            .forEach(line -> {
               String[] parts = line.trim().split("\\s+");
               if (parts.length != 2) {
                  System.out.println("Invalid line: '" + line + "'. Skipping...");
                  return;
               }

               try {
                  int left = Integer.parseInt(parts[0]);
                  int right = Integer.parseInt(parts[1]);

                  // Add numbers to respective lists
                  leftNumbers.add(left);
                  rightNumbers.add(right);
               } catch (NumberFormatException e) {
                  System.out.println("Invalid numbers in line: '" + line + "'. Skipping...");
               }
            });

      // Sort the lists
      leftNumbers.sort(Integer::compareTo);
      rightNumbers.sort(Integer::compareTo);
   }

   private static int calculateSumOfDistances(List<Integer> leftNumbers, List<Integer> rightNumbers) {
      if (leftNumbers.size() != rightNumbers.size()) {
         throw new IllegalArgumentException("Lists must have the same size to calculate distances.");
      }

      return IntStream.range(0, leftNumbers.size())
            .map(i -> Math.abs(leftNumbers.get(i) - rightNumbers.get(i)))
            .sum();
   }
}
