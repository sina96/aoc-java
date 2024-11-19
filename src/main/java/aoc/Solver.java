package aoc;

import java.util.Scanner;

public class Solver {

   public static void main(String[] args) {
      try (Scanner scanner = new Scanner(System.in)) {
         System.out.print("Enter the year (e.g., 2024): ");
         int year = scanner.nextInt();
         System.out.print("Enter the day to solve (e.g., 1 for Day 1): ");
         int day = scanner.nextInt();

         String inputFileName = String.format("inputs/year%d/day%02d.txt", year, day);
         InputLoader loader = new InputLoader();
         var input = loader.getInputForDay(inputFileName);

         // Use reflection to load the correct year and day class
         String className = String.format("aoc.year%d.Day%02d", year, day);
         try {
            Class<?> dayClass = Class.forName(className);
            var constructor = dayClass.getConstructor(java.util.List.class);
            var dayInstance = constructor.newInstance(input);
            dayClass.getMethod("solve").invoke(dayInstance);
         } catch (Exception e) {
            System.out.println("Solution for this day/year is not implemented yet.");
            e.printStackTrace();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
