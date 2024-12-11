package aoc.year2024;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Day11
{
   private final List<String> input;
   private final Map<String, Long> memo = new HashMap<>();

   public Day11(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      final List<Long> stoneNumbers = Arrays.stream(input.get(0).split(" "))
            .map(Long::parseLong)
            .collect(Collectors.toList());

      long partOneResult = calculateTotalStoneSize(stoneNumbers, 25);

      System.out.println("Part 1: "+ partOneResult);

      long partOTwoResult = calculateTotalStoneSize(stoneNumbers, 75);
      System.out.println("Part 2: " + partOTwoResult);

   }

   private long calculateTotalStoneSize(List<Long> stoneNumbers, int blinks)
   {
      long result = 0;
      for (long stone : stoneNumbers) {
         result += calculateSizeEvolutionForSingleStone(blinks, stone);
      }
      return result;
   }

   private long calculateSizeEvolutionForSingleStone(int blinks, long stone) {
      // Generate memoization key based on input parameters
      String memoKey = getMemoKey(blinks, stone);

      // Check if the result is already memoized
      if (memo.containsKey(memoKey)) {
         return memo.get(memoKey);
      }

      // Base cases
      if (blinks == 0) {
         return 1;
      }
      if (stone == 0) {
         long result = calculateSizeEvolutionForSingleStone(blinks - 1, 1L);
         memo.put(memoKey, result); // Memoize result
         return result;
      }

      long result;
      // Process based on whether the stone has an even number of digits
      if (hasEvenNrDigits(stone)) {
         Long[] splitResult = split(stone);
         result = calculateSizeEvolutionForSingleStone(blinks - 1, splitResult[0]) +
               calculateSizeEvolutionForSingleStone(blinks - 1, splitResult[1]);
      } else {
         result = calculateSizeEvolutionForSingleStone(blinks - 1, stone * 2024L);
      }

      // Memoize and return the result
      memo.put(memoKey, result);
      return result;
   }



   private boolean hasEvenNrDigits(Long stone)
   {
      int nrOfDigits = getNrOfDigits(stone);
      return nrOfDigits % 2 == 0;
   }

   private int getNrOfDigits(Long stone) {
      if (stone == 0) {
         return 1;
      }
      return (int) Math.log10(Math.abs(stone)) + 1;
   }


   private Long[] split(final Long stone)
   {
      int nrOfDigits = getNrOfDigits(stone);
      final int splitAfter = nrOfDigits / 2;
      Long[] result = new Long[2];
      result[0] = stone / (long) Math.pow(10, splitAfter);
      result[1] = stone % (long) Math.pow(10, splitAfter);
      return result;
   }

   private static String getMemoKey(int blinks, long stone)
   {
      return blinks + "_" + stone;
   }
}

