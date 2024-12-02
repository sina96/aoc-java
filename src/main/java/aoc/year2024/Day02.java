package aoc.year2024;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Day02 {
   private final List<String> input;
   private final List<Boolean> checklist = new ArrayList<>();

   public Day02(List<String> input) {
      this.input = input;
   }

   public void solve() {

      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      processInput(input, checklist);

      System.out.println("Number of safe lines are: " + checklist.stream().filter(Boolean::booleanValue).count());
   }

   private static void processInput(List<String> input, List<Boolean> checklist) {
      input.stream()
            .map(String::trim) // Trim each line
            .filter(line -> !line.isEmpty()) // Filter out empty or whitespace-only lines
            .map(line -> {
               try {
                  // Parse numbers in the line
                  return Stream.of(line.split("\\s+"))
                        .map(Integer::parseInt)
                        .toList();
               } catch (NumberFormatException e) {
                  System.out.println("Invalid numbers in line: '" + line + "'. Skipping...");
                  return null; // Return null for invalid lines
               }
            })
            .filter(Objects::nonNull) // Exclude null results
            .forEach(lineNumbers -> checklist.add(isASafeLine(lineNumbers)));
   }

   private static Boolean isASafeLine(List<Integer> lineNumbers)
   {
      if (lineNumbers == null || lineNumbers.size() < 2) {
         return false;
      }

      // Calculate differences between consecutive numbers
      List<Integer> differences = IntStream.range(0, lineNumbers.size() - 1)
            .mapToObj(i -> lineNumbers.get(i + 1) - lineNumbers.get(i))
            .toList();

      // Check if all differences are within the allowed range
      boolean allDifferencesValid = differences.stream()
            .allMatch(diff -> Math.abs(diff) >= 1 && Math.abs(diff) <= 3);

      if (!allDifferencesValid) {
         return false; // Differences are not in the allowed range
      }

      // Check if the trend is consistently increasing or decreasing
      boolean isIncreasing = differences.stream().allMatch(diff -> diff > 0);
      boolean isDecreasing = differences.stream().allMatch(diff -> diff < 0);

      return isIncreasing || isDecreasing; // Return true if consistent trend
   }
}
