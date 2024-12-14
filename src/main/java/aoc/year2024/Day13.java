package aoc.year2024;

import java.util.ArrayList;
import java.util.List;


public class Day13
{
   private final List<String> input;

   public Day13(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      List<Equation> equationList = fillEquationList(input);

      long totalTokensPartOne = calculateTotalTokens(equationList, 0L);

      System.out.println("Part 1: "+ totalTokensPartOne);

      long totalTokensPartTwo = calculateTotalTokens(equationList, 10000000000000L);

      System.out.println("Part 2: "+ totalTokensPartTwo);

   }

   private List<Equation> fillEquationList(List<String> input)
   {
      final List<Equation> result = new ArrayList<>();
      List<String> current = new ArrayList<>();
      for (int i = 0; i < input.size(); i++) {
         final String line = input.get(i);
         if (line.isBlank()) {
            result.add(parseToEquation(current));
            current = new ArrayList<>();
         } else {
            current.add(input.get(i));
            if (i == input.size() - 1) {
               result.add(parseToEquation(current));
            }
         }
      }
      return result;
   }

   private long calculateTotalTokens(List<Equation> equationList, long prizeCalibration)
   {
      long totalTokens = 0;
      for (Equation eq : equationList) {
         try {
            if(prizeCalibration != 0L)
            {
               eq.calibratePrizes(prizeCalibration);
            }
            double[] solution = solveEquation(eq);
            long a = Math.round(solution[0]);
            long b = Math.round(solution[1]);
            // Check if both solutions are natural numbers
            if (eq.checkAnswers(a,b)) {
               totalTokens += 3 * a + b;
            }
         } catch (IllegalArgumentException e) {
            System.out.println("Error solving equation: " + e.getMessage());
         }
      }

      return totalTokens;
   }

   private double[] solveEquation(Equation eq)
   {
      double[][] coefficients = {
            {eq.xa, eq.xb},
            {eq.ya, eq.yb}
      };
      double[] constants = {eq.xPrize, eq.yPrize};

      int n = constants.length;
      double[] solution = new double[n];

      // Calculate the determinant of the coefficient matrix
      double det = coefficients[0][0] * coefficients[1][1] - coefficients[0][1] * coefficients[1][0];
      if (det == 0) {
         throw new IllegalArgumentException("The system of equations has no unique solution (determinant is zero).");
      }

      // Calculate the inverse of the coefficient matrix
      double[][] inverse = new double[2][2];
      inverse[0][0] = coefficients[1][1] / det;
      inverse[0][1] = -coefficients[0][1] / det;
      inverse[1][0] = -coefficients[1][0] / det;
      inverse[1][1] = coefficients[0][0] / det;

      // Multiply the inverse of the coefficient matrix by the constants vector
      for (int i = 0; i < 2; i++) {
         for (int j = 0; j < 2; j++) {
            solution[i] += inverse[i][j] * constants[j];
         }
      }

      return solution;
   }

   private Equation parseToEquation(final List<String> input) {
      final String line1 = input.get(0);
      final String line2 = input.get(1);
      final String line3 = input.get(2);

      final int xa = Integer.parseInt(line1.split("X+")[1].split(",")[0]);
      final int ya = Integer.parseInt(line1.split("Y+")[1]);
      final int xb = Integer.parseInt(line2.split("X+")[1].split(",")[0]);
      final int yb = Integer.parseInt(line2.split("Y+")[1]);
      final int xPrize = Integer.parseInt(line3.split("X=")[1].split(",")[0]);
      final int yPrize = Integer.parseInt(line3.split("Y=")[1]);

      return new Equation(xa, xb, xPrize, ya, yb, yPrize);
   }

   static class Equation {
      int xa;
      int xb;
      long xPrize;
      int ya;
      int yb;
      long yPrize;

      // Constructor to initialize the equation
      public Equation(int xa, int xb, int xPrize, int ya, int yb, int yPrize) {
         this.xa = xa;
         this.xb = xb;
         this.xPrize = xPrize;
         this.ya = ya;
         this.yb = yb;
         this.yPrize = yPrize;
      }

      public boolean checkAnswers(long ansA, long ansB)
      {
         return ansA >=0 && ansB >= 0 &&
               ansA * xa + ansB * xb == xPrize && ansA * ya + ansB * yb == yPrize;
      }

      public void calibratePrizes(long prizeCalibration)
      {
         this.xPrize = xPrize + prizeCalibration;
         this.yPrize = yPrize + prizeCalibration;
      }
   }
}

