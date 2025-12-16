package aoc.year2025;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Day03
{
   private final List<String> input;

   List<String> batteries = new ArrayList<>();

   public Day03(List<String> input) {
      this.input = input;
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      batteries = input;

      long sum1 = calculateJoltageSum(batteries, 2);
      System.out.println("Part 1: " + sum1);

      long sum2 = calculateJoltageSum(batteries, 12);
      System.out.println("Part 2: " + sum2);
   }

   private long calculateJoltageSum(List<String> batteries, int length)
   {
      if(batteries.isEmpty())
      {
         return 0L;
      }
      long sum = 0;
      for(String battery : batteries)
      {
         List<Character> charList = battery.chars()
               .mapToObj(c -> (char) c)
               .collect(Collectors.toList());

         sum += Long.parseLong(getBatteryNumber(charList, length));
      }

      return sum;
   }

   private String getBatteryNumber(List<Character> charList, int k) {
      int n = charList.size();
      if (k < 1) throw new IllegalArgumentException("k must be >= 1");
      if (n < k) throw new IllegalArgumentException("Need at least k characters");

      StringBuilder out = new StringBuilder(k);
      int prevIndex = -1;

      for (int pos = 0; pos < k; pos++) {
         int start = prevIndex + 1;

         // Latest index we are allowed to pick at this position
         // (must leave room for remaining digits)
         int maxIndexInclusive = n - k + pos;

         if (start > maxIndexInclusive) {
            throw new IllegalArgumentException("Not enough characters left to build " + k + " digits");
         }

         int chosenIndex = java.util.stream.IntStream.rangeClosed(start, maxIndexInclusive)
               .boxed()
               .max(
                     java.util.Comparator
                           .comparingInt((Integer i) -> Character.getNumericValue(charList.get(i)))
                           // tie-break: prefer smaller index (earlier), to keep more room later
                           .thenComparingInt(i -> -i)
               )
               .orElseThrow();

         out.append(charList.get(chosenIndex));
         prevIndex = chosenIndex;
      }

      return out.toString();
   }
}