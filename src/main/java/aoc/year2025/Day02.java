package aoc.year2025;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Day02
{
   private final List<String> input;

   List<String> ranges = new ArrayList<>();



   public Day02(List<String> input) {
      this.input = input;
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }


      ranges = processInput(input);

      long countInvalids = solvePart1(ranges);
      long countInvalidsAtLeastTwice = solvePart2(ranges);

      System.out.println("Part 1: " + countInvalids);
      System.out.println("Part 2: " + countInvalidsAtLeastTwice);
   }

   private long solvePart1(List<String> ranges)
   {
      List<Long> invalidsIds = new ArrayList<>();
      for (String range : ranges) {
         String[] rangeNumbers = range.split("-");
         long start = Long.parseLong(rangeNumbers[0]);
         long end = Long.parseLong(rangeNumbers[1]);

         for(long l = start; l <= end; l++){
            if(isInvalidId(Long.toString(l)))
               invalidsIds.add(l);
         }

      }

      return invalidsIds.stream().mapToLong(Long::longValue).sum();
   }

   private long solvePart2(List<String> ranges)
   {
      List<Long> invalidsIds = new ArrayList<>();
      for (String range : ranges) {
         String[] rangeNumbers = range.split("-");
         long start = Long.parseLong(rangeNumbers[0]);
         long end = Long.parseLong(rangeNumbers[1]);

         for(long l = start; l <= end; l++){
            if(isInvalidIdAtLeastTwice(Long.toString(l)))
               invalidsIds.add(l);
         }

      }

      return invalidsIds.stream().mapToLong(Long::longValue).sum();
   }

   private boolean isInvalidId(String id)
   {
      if (id.length() % 2 != 0)
         return false;

      int half = id.length() / 2;
      String first = id.substring(0, half);
      String second = id.substring(half);

      return first.equals(second);
   }

   private boolean isInvalidIdAtLeastTwice(String id)
   {
      int length = id.length();

      for (int i = 1; i <= length /2; i++) {
         if(length % i != 0)
            continue;
         String pattern = id.substring(0,i);

         boolean isRepeating = true;

         int j = i;
         while (j < length) {
            String block = id.substring(j, j+i);
            if(!block.equals(pattern))
            {
               isRepeating = false;
               break;
            }
            j = j+i;
         }
         if (isRepeating)
            return true;
      }
      return false;
   }

   private static List<String> processInput(List<String> input) {
      return Arrays.asList(input.get(0).split(","));
   }
}