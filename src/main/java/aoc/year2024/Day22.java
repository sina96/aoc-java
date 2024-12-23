package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class Day22
{
   private final List<String> input;


   public Day22(List<String> input)
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
      List<Long> numbers = parseNumbers(input);

      long partOneResult = calculateTotalSecretNumber(numbers, 2000);

      System.out.println("Part 1: " + partOneResult);

   }

   private void solvePartTwo()
   {
      List<Long> numbers = parseNumbers(input);
      Map<Long, Map<List<Integer>, Integer>> fourSequencesInNumbersMap = getFourSequencesInNumbersMap(numbers);
      Set<List<Integer>> allSequences = getAllSequences(fourSequencesInNumbersMap);
      
      int maxBananas = getMaxBananas(numbers, fourSequencesInNumbersMap, allSequences);

      System.out.println("Part 2:" + maxBananas);
   }

   private int getMaxBananas(List<Long> numbers, Map<Long, Map<List<Integer>, Integer>> fourSequencesInNumbersMap,
         Set<List<Integer>> allSequences)
   {
      int maxBananas = 0;
      for (final List<Integer> sequence : allSequences) {
         int bananas = 0;
         for (final long secret : numbers) {
            final Map<List<Integer>, Integer> sequences = fourSequencesInNumbersMap.get(secret);
            bananas += sequences.getOrDefault(sequence, 0);
         }
         maxBananas = Math.max(maxBananas, bananas);
      }
      return maxBananas;
   }

   private Set<List<Integer>> getAllSequences(final Map<Long, Map<List<Integer>, Integer>> sequencesToScores) {
      return sequencesToScores.values().stream()
            .flatMap(s -> s.keySet().stream())
            .collect(Collectors.toSet());
   }

   private Map<Long, Map<List<Integer>, Integer>> getFourSequencesInNumbersMap(List<Long> numbers)
   {
      Map<Long, Map<List<Integer>, Integer>> fourSequencesInNumbers = new HashMap<>();

      for(long secret : numbers) {
         Map<List<Integer>, Integer> mapValue = getFourSequenceToFirstPrice(secret);
         fourSequencesInNumbers.put(secret, mapValue);
      }
      return fourSequencesInNumbers;
   }

   private Map<List<Integer>, Integer> getFourSequenceToFirstPrice(long secret)
   {
      final Map<List<Integer>, Integer> seqToPrice = new HashMap<>();
      List<Integer> prevDifferences = new ArrayList<>();
      int prevPrice = getPrice(secret);
      for (int i = 1; i <= 2000; i++)
      {
         secret = performProcess(secret);
         final int price = getPrice(secret);
         final int difference = price - prevPrice;
         prevDifferences.add(difference);
         if (prevDifferences.size() == 4) {
            seqToPrice.putIfAbsent(prevDifferences, price);
            prevDifferences = new ArrayList<>(prevDifferences.subList(1, prevDifferences.size()));
         }
         prevPrice = price;
      }
      return seqToPrice;
   }

   private int getPrice(long secret)
   {
      return (int) (secret % 10);
   }

   private long calculateTotalSecretNumber(List<Long> numbers, int rounds)
   {
      final Map<String, Long> memo = new HashMap<>();
      long result = 0;
      for (long number : numbers) {
         result += calculateSingleNumberSecret(number, rounds, memo);
      }
      return result;
   }

   private long calculateSingleNumberSecret(long number, int rounds, Map<String, Long> memo)
   {
      // Generate memoization key based on input parameters
      String memoKey = getMemoKey(number, rounds);

      // Check if the result is already memoized
      if (memo.containsKey(memoKey)) {
         return memo.get(memoKey);
      }

      // Base cases
      if (rounds == 0) {
         return number;
      }

      number = performProcess(number);
      memo.put(memoKey, number);
      return calculateSingleNumberSecret(number, rounds -1, memo);
   }

   private long performProcess(long number)
   {
      long result = number << 6;
      number = result ^ number;
      number = number % 16777216;
      result = number >> 5;
      number = result ^ number;
      number = number % 16777216;
      result = number << 11;
      number = result ^ number;
      number = number % 16777216;
      return number;
   }

   private List<Long> parseNumbers(List<String> input)
   {
      return input.stream().map(Long::parseLong).collect(Collectors.toList());
   }

   private static String getMemoKey(long number, int rounds)
   {
      return number + "_" + rounds ;
   }

}

