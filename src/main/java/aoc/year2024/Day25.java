package aoc.year2024;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Day25
{
   private final List<String> input;

   public Day25(List<String> input)
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
      final List<Lock> locks = getLocks(input);
      final List<Key> keys = getKeys(input);

      long totalFit = keys.stream()
            .flatMap(key -> locks.stream().filter(key::fits))
            .count();

      System.out.println("Part1: "+ totalFit );
   }

   private void solvePartTwo()
   {
      System.out.println("Goodjob! Merry XMAS!");
   }

   private List<Lock> getLocks(final List<String> input) {
      final List<Lock> result = new ArrayList<>();
      boolean firstLine = true;

      for (int i = 0; i < input.size(); i++) {
         final String line = input.get(i);

         if (firstLine) {
            if (line.equals("#####")) {
               final int[] pins = calculatePinsForLocks(input, i);
               result.add(new Lock(pins[0], pins[1], pins[2], pins[3], pins[4]));
            }
            firstLine = false;
         }

         if (line.isBlank()) {
            firstLine = true;
         }
      }

      return result;
   }

   private int[] calculatePinsForLocks(final List<String> input, int startIndex) {
      final int[] pins = new int[5];

      for (int x = 0; x < input.get(startIndex).length(); x++) {
         int pin = 0;
         for (int y = startIndex + 1; y <= startIndex + 6; y++) {
            if (input.get(y).charAt(x) == '#') {
               pin++;
            } else {
               break;
            }
         }
         pins[x] = pin;
      }

      return pins;
   }

   private List<Key> getKeys(final List<String> input) {
      final List<Key> result = new ArrayList<>();
      boolean firstLine = true;

      for (int i = 0; i < input.size(); i++) {
         final String line = input.get(i);

         if (firstLine) {
            if (input.get(i + 6).equals("#####")) {
               final int[] pins = calculatePinsForKeys(input, i);
               result.add(new Key(pins[0], pins[1], pins[2], pins[3], pins[4]));
            }
            firstLine = false;
         }

         if (line.isBlank()) {
            firstLine = true;
         }
      }

      return result;
   }

   private int[] calculatePinsForKeys(final List<String> input, int startIndex) {
      final int[] pins = new int[5];

      for (int x = 0; x < input.get(startIndex).length(); x++) {
         int pin = 0;
         for (int y = startIndex + 5; y > startIndex; y--) {
            if (input.get(y).charAt(x) == '#') {
               pin++;
            } else {
               break;
            }
         }
         pins[x] = pin;
      }

      return pins;
   }

   static final class Lock
   {
      private final int pin1;
      private final int pin2;
      private final int pin3;
      private final int pin4;
      private final int pin5;

      Lock(int pin1, int pin2, int pin3, int pin4, int pin5)
      {
         this.pin1 = pin1;
         this.pin2 = pin2;
         this.pin3 = pin3;
         this.pin4 = pin4;
         this.pin5 = pin5;
      }

      @Override
      public String toString()
      {
         return "Lock[" + "pin1=" + pin1 + ", " + "pin2=" + pin2 + ", " + "pin3=" + pin3 + ", " + "pin4=" + pin4 + ", " + "pin5="
               + pin5 + ']';
      }

      public int pin1()
      {
         return pin1;
      }

      public int pin2()
      {
         return pin2;
      }

      public int pin3()
      {
         return pin3;
      }

      public int pin4()
      {
         return pin4;
      }

      public int pin5()
      {
         return pin5;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
            return true;
         if (obj == null || obj.getClass() != this.getClass())
            return false;
         var that = (Lock) obj;
         return this.pin1 == that.pin1 && this.pin2 == that.pin2 && this.pin3 == that.pin3 && this.pin4 == that.pin4
               && this.pin5 == that.pin5;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(pin1, pin2, pin3, pin4, pin5);
      }


   }

   static final class Key
   {
      private final int pin1;
      private final int pin2;
      private final int pin3;
      private final int pin4;
      private final int pin5;

      Key(int pin1, int pin2, int pin3, int pin4, int pin5)
      {
         this.pin1 = pin1;
         this.pin2 = pin2;
         this.pin3 = pin3;
         this.pin4 = pin4;
         this.pin5 = pin5;
      }

      boolean fits(final Lock l)
      {
            final int c1 = pin1 + l.pin1();
            final int c2 = pin2 + l.pin2();
            final int c3 = pin3 + l.pin3();
            final int c4 = pin4 + l.pin4();
            final int c5 = pin5 + l.pin5();
            return c1 <= 5 && c2 <= 5 && c3 <= 5 && c4 <= 5 && c5 <= 5;
         }

      public int pin1()
      {
         return pin1;
      }

      public int pin2()
      {
         return pin2;
      }

      public int pin3()
      {
         return pin3;
      }

      public int pin4()
      {
         return pin4;
      }

      public int pin5()
      {
         return pin5;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
            return true;
         if (obj == null || obj.getClass() != this.getClass())
            return false;
         var that = (Key) obj;
         return this.pin1 == that.pin1 && this.pin2 == that.pin2 && this.pin3 == that.pin3 && this.pin4 == that.pin4
               && this.pin5 == that.pin5;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(pin1, pin2, pin3, pin4, pin5);
      }

      @Override
      public String toString()
      {
         return "Key[" + "pin1=" + pin1 + ", " + "pin2=" + pin2 + ", " + "pin3=" + pin3 + ", " + "pin4=" + pin4 + ", " + "pin5="
               + pin5 + ']';
      }

      }
}

