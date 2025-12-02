package aoc.year2025;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Day01
{
   private final List<String> input;

   String[] sides;
   int[] numbers;

   public Day01(List<String> input) {
      this.input = input;
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      sides = new String[input.size()];
      numbers = new int[input.size()];
      processInput(input, sides, numbers);

      int passwordPartOne = solvePart1(sides, numbers, 50);
      int passwordPartTwo = solvePart2(sides, numbers, 50);

      System.out.println("Part 1: " + passwordPartOne);
      System.out.println("Part 2: " + passwordPartTwo);
   }

   private int solvePart1(String[] sides, int[] numbers, int start)
   {
      int current = start;
      int next;
      int zeroReached = 0;
      for (int i = 0; i < numbers.length; i++)
      {
         int applier = numbers[i];
         int fullRounds = applier / 100;
         applier = applier - (100 * fullRounds);

         if (sides[i].equals("R")){
            next = applier + current;

            if(next > 99)
            {
               next = next - 100;
            }
         }
         else
         {
            next  = current - applier;
            if (next < 0)
            {
               next = 100 + next;
            }
         }
         if (next == 0)
            zeroReached++;

         current = next;
      }
      return zeroReached;
   }

   private int solvePart2(String[] sides, int[] numbers, int start)
   {
      int current = start;
      int next;
      int zeroPassed = 0;
      for (int i = 0; i < numbers.length; i++)
      {
         int applier = numbers[i];

         int fullRounds = applier / 100;
         zeroPassed += fullRounds;
         applier = applier - (100 * fullRounds);

         if (sides[i].equals("R"))
         {
            next = applier + current;

            if(next > 99)
            {
               zeroPassed++;
               next = next - 100;
            }
         }else
         {
            next  = current - applier;
            if (next < 0)
            {
               if (current != 0)
               {
                  zeroPassed++;
               }

               next = 100 + next;
            }
            if(next == 0)
            {
               zeroPassed++;
            }
         }
         current = next;
      }
      return zeroPassed;
   }

   private static void processInput(List<String> input, String[] sides, int[] numbers) {
      Pattern pattern = Pattern.compile("([LR])(\\d+)");
      for (int i = 0; i < input.size(); i++) {
         Matcher matcher = pattern.matcher(input.get(i));
         if (matcher.matches()) {
            sides[i] = matcher.group(1);
            numbers[i] = Integer.parseInt(matcher.group(2));
         }
      }
   }
}
