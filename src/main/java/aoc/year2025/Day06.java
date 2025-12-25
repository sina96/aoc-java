package aoc.year2025;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Day06
{
   private final List<String> input;

   private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
   private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+*]");


   public Day06(List<String> input) {
      this.input = input;
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      long partOneSolution = partOneOperation();
      System.out.println("Part 1: " + partOneSolution);

      long partTwoSolution = partTwoOperation();
      System.out.println("Part 2: " + partTwoSolution);
   }

   private long partOneOperation()
   {
      List<List<Integer>> numbers = extractNumbers(input);
      List<String> operators = extractOperators(input);
      long sum = 0;

       for(int column=0; column < operators.size(); column++)
       {
          String operator = operators.get(column);
          long result = numbers.get(0).get(column);
          for(int row=1; row < numbers.size(); row++)
          {
             Integer operand = numbers.get(row).get(column);
             if(operator.equals("+"))
             {
                result +=  operand;
             }
             else
             {
                result *= operand;
             }
          }
          sum += result;
       }
       return sum;
   }

   private long partTwoOperation() {
      final int height = input.size();
      final int digitRows = height - 1;

      /*
       * PART TWO INTERPRETATION
       *
       * - The input is treated as a fixed-width character grid.
       * - Each arithmetic "problem" is defined by a contiguous block of columns.
       * - Problems are separated by a column that contains ONLY spaces
       *   in every row (including the operator row).
       *
       * - Within a problem:
       *     • Each column represents ONE operand.
       *     • The operand is read vertically, top-to-bottom,
       *       using all rows except the operator row.
       *     • Leading spaces are ignored when parsing the number.
       *
       * - The operator for the entire problem is the symbol located
       *   in the BOTTOM ROW at the LEFTMOST column of the problem.
       */

      // Ensure all rows have equal width so column indexing is consistent.
      final int width = input.stream().mapToInt(String::length).max().orElse(0);
      final List<String> grid = new ArrayList<>(height);
      for (String row : input) {
         grid.add(padRight(row, width));
      }

      final String operatorRow = grid.get(height - 1);

      /*
       * Identify separator columns.
       * A column is considered a separator if it contains only spaces
       * in every row. These columns divide the worksheet into problems.
       */
      final boolean[] isSeparatorColumn = new boolean[width];
      for (int col = 0; col < width; col++) {
         boolean blank = true;
         for (int row = 0; row < height; row++) {
            if (grid.get(row).charAt(col) != ' ') {
               blank = false;
               break;
            }
         }
         isSeparatorColumn[col] = blank;
      }

      long sum = 0L;
      int col = 0;

      /*
       * Scan left-to-right across the grid, extracting one problem
       * (one contiguous block of non-separator columns) at a time.
       */
      while (col < width) {
         // Skip separator columns between problems.
         while (col < width && isSeparatorColumn[col]) col++;
         if (col >= width) break;

         // Start of the current problem.
         final int startCol = col;

         // Advance until the next separator column.
         while (col < width && !isSeparatorColumn[col]) col++;
         final int endColExclusive = col;

         /*
          * OPERATOR SELECTION (Part Two rule):
          * The operator for this problem is the character in the
          * bottom row at the LEFTMOST column of the problem.
          */
         final char operator = operatorRow.charAt(startCol);
         if (operator != '+' && operator != '*') {
            throw new IllegalArgumentException(
                  "Unexpected operator '" + operator + "' at column " + startCol);
         }

         /*
          * OPERAND SELECTION (Part Two rule):
          * Each column within the problem range corresponds to
          * one operand. The operand is formed by reading the
          * column vertically from top to bottom, excluding
          * the operator row.
          */
         long result = 0L;
         boolean firstOperand = true;

         for (int operandCol = startCol; operandCol < endColExclusive; operandCol++) {
            StringBuilder digits = new StringBuilder(digitRows);

            // Build the vertical number for this operand.
            for (int row = 0; row < digitRows; row++) {
               digits.append(grid.get(row).charAt(operandCol));
            }

            int operand = Integer.parseInt(digits.toString().trim());

            // Fold operands using the problem's operator.
            if (firstOperand) {
               result = operand;
               firstOperand = false;
            } else if (operator == '+') {
               result += operand;
            } else { // '*'
               result *= operand;
            }
         }

         // Add this problem's result to the grand total.
         sum += result;
      }

      return sum;
   }

   private static String padRight(String s, int width) {
      if (s.length() >= width) return s;
      return s + " ".repeat(width - s.length());
   }

   private List<String> extractOperators(List<String> input)
   {
     String operatorLine = input.get(input.size() - 1);

      return OPERATOR_PATTERN.matcher(operatorLine)
            .results()
            .map(MatchResult::group)
            .collect(Collectors.toList());
   }

   public static List<List<Integer>> extractNumbers(List<String> input)
   {
      return input.stream()
            .map(line ->
                  NUMBER_PATTERN.matcher(line)
                        .results()
                        .map(match -> Integer.parseInt(match.group()))

                        .collect(Collectors.toList())
            )
            .filter(list -> !list.isEmpty())
            .collect(Collectors.toList());
   }

}

