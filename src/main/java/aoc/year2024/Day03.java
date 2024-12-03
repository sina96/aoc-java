package aoc.year2024;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Day03 {
   private final List<String> input;
   private static final String regex = "mul\\((\\d+),(\\d+)\\)";
   private static final Pattern pattern = Pattern.compile(regex);

   private static final String regexWithFlags = "mul\\(\\d+,\\d+\\)|do\\(\\)|don't\\(\\)";
   private static final Pattern patternWithFlags = Pattern.compile(regexWithFlags);

   public Day03(List<String> input) {
      this.input = input;
   }

   public void solve() {

      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      long sumOfMultiplications = processInputAndCalculateMulSum(input, pattern);
      System.out.println("Total Sum of all multiplications: " + sumOfMultiplications);

      long sumOfFilteredMultiplications = processInputWithFlagsAndCalculateMulSum(input, patternWithFlags);
      System.out.println("Total Sum of all refined multiplications: " + sumOfFilteredMultiplications);
   }

   private static long processInputAndCalculateMulSum(List<String> input, Pattern pattern) {
      return input.stream()
            .flatMap(line -> pattern.matcher(line).results()) // Get matches as a stream
            .mapToLong(match -> {
               int number1 = Integer.parseInt(match.group(1));
               int number2 = Integer.parseInt(match.group(2));
               return (long) number1 * number2; // Calculate product
            })
            .sum(); // Sum up all products
   }

   private static long processInputWithFlagsAndCalculateMulSum(List<String> input, Pattern pattern) {
      boolean[] shouldSkip = {false}; // Use an array to hold the boolean since lambda requires effectively final variables
      return input.stream()
            .flatMap(line -> {
               Matcher matcher = pattern.matcher(line);
               List<String> matches = new ArrayList<>();
               while (matcher.find())
               {
                  matches.add(matcher.group());
               }
               return matches.stream();
            })
            .mapToLong(match -> {
               if (match.equals("don't()")) {
                  shouldSkip[0] = true;
                  // System.out.println("Found 'don't()', skipping further calculations.");
                  return 0;
               } else if (match.equals("do()")) {
                  shouldSkip[0] = false;
                  // System.out.println("Found 'do()', resuming calculations.");
                  return 0;
               } else if (!shouldSkip[0]) {
                  // Extract numbers within mul(...)
                  String[] numbers = match.substring(4, match.length() - 1).split(",");
                  if (numbers.length == 2) {
                     try {
                        int number1 = Integer.parseInt(numbers[0].trim());
                        int number2 = Integer.parseInt(numbers[1].trim());
                        // System.out.printf("Processing %s => Product: %d%n", match, product);
                        return (long) number1 * number2;
                     } catch (NumberFormatException e) {
                        // System.err.println("Invalid number format in " + Arrays.toString(numbers));
                     }
                  } else {
                     System.err.println("Unexpected format for multiplication in: " + match);
                  }
               }
               return 0;
            })
            .sum();
   }
}
