package aoc.year2024;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Day07
{
   private final List<String> input;

   public Day07(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      long sumOfCorrectLinesTwoOperations = calculateSumOfCorrectLines(input, false);
      System.out.println("Part 1: " + sumOfCorrectLinesTwoOperations);

      long sumOfCorrectLinesThreeOperations = calculateSumOfCorrectLines(input, true);
      System.out.println("Part 2: " + sumOfCorrectLinesThreeOperations);
   }

   private long calculateSumOfCorrectLines(List<String> input, boolean isThirdOperationUnlocked)
   {
      return input.stream()
            .map(line -> line.split(":"))
            .filter(parts -> parts.length == 2)
            .mapToLong(parts -> {
               long key = Long.parseLong(parts[0]);
               List<Long> values = Arrays.stream(parts[1].trim().split(" "))
                     .map(Long::parseLong)
                     .toList();

               Set<Long> possibleResults = calculateResults(values, key, isThirdOperationUnlocked);
               return possibleResults.contains(key) ? key : 0L;
            })
            .sum();
   }

   private static Set<Long> calculateResults(List<Long> numbers, Long targetKey, boolean isThirdOperationUnlocked) {
      if (numbers == null || numbers.size() < 2) {
         throw new IllegalArgumentException("List must contain at least two numbers.");
      }

      Set<Long> results = new HashSet<>();
      generateResults(numbers, 0, (long) numbers.get(0), results, targetKey, isThirdOperationUnlocked);
      return results;
   }

   private static void generateResults(List<Long> numbers, int index, long currentResult, Set<Long> results, Long targetKey, boolean isThirdOperationUnlocked) {
      if (index == numbers.size() - 1 || targetKey.compareTo(currentResult) < 0) {
         results.add(currentResult);
         return;
      }

      long nextNumber = numbers.get(index + 1);

      // Perform addition
      generateResults(numbers, index + 1, currentResult + nextNumber, results, targetKey, isThirdOperationUnlocked);

      // Perform multiplication
      generateResults(numbers, index + 1, currentResult * nextNumber, results, targetKey, isThirdOperationUnlocked);

      // Perform concatenation if available
      if(isThirdOperationUnlocked){
         generateResults(numbers, index + 1, concatenateOperation(currentResult, nextNumber), results, targetKey, isThirdOperationUnlocked);
      }
   }

   private static long concatenateOperation(long firstNumber, long nextNumber)
   {
      // Count the number of digits in nextNumber
      long nextNumberCopy = nextNumber;
      int digits = 0;
      while (nextNumberCopy > 0) {
         nextNumberCopy /= 10;
         digits++;
      }

      // Shift firstNumber by the number of digits and add nextNumber
      return firstNumber * (long)Math.pow(10, digits) + nextNumber;
   }
}
