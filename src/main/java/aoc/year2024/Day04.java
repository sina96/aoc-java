package aoc.year2024;

import java.util.List;


public class Day04
{
   private final List<String> input;
   private static final String searchWord = "XMAS";

   public Day04(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      validateGrid(input);

      int countXMASWord = countXMASWordOccurrences(searchWord, input);

      // Print the result
      System.out.printf("The word \"%s\" occurs %d times in the grid.%n", searchWord, countXMASWord);

      int countXShapeMAS = countXShapeMAS(input);
      System.out.printf("Total occurrences of X-shaped MAS: %d%n", countXShapeMAS);
   }

   private void validateGrid(List<String> grid)
   {
      int length = grid.get(0).length();
      for (String row : grid) {
         if (row.length() != length) {
            throw new IllegalArgumentException("All rows in the grid must have the same length.");
         }
      }
   }

   private int countXMASWordOccurrences(String word, List<String> gridData) {
      int rows = gridData.size();
      int cols = gridData.get(0).length();
      int wordLength = word.length();
      int count = 0;

      char[][] gridArray = convertToGridArray(gridData);

      int[][] directions = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0},
            {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
      };

      for (int row = 0; row < rows; row++) {
         for (int col = 0; col < cols; col++) {
            for (int[] direction : directions) {
               if (matchesWord(gridArray, word, row, col, direction, wordLength)) {
                  count++;
               }
            }
         }
      }
      return count;
   }

   private char[][] convertToGridArray(List<String> grid) {
      int rows = grid.size();
      int cols = grid.get(0).length();
      char[][] gridArray = new char[rows][cols];
      for (int i = 0; i < rows; i++) {
         gridArray[i] = grid.get(i).toCharArray();
      }
      return gridArray;
   }

   private boolean matchesWord(char[][] grid, String word, int row, int col, int[] direction, int wordLength) {
      int rows = grid.length;
      int cols = grid[0].length;
      for (int i = 0; i < wordLength; i++) {
         if (row < 0 || row >= rows || col < 0 || col >= cols || grid[row][col] != word.charAt(i)) {
            return false;
         }
         row += direction[0];
         col += direction[1];
      }
      return true;
   }

   private static int countXShapeMAS(List<String> grid) {
      int rows = grid.size();
      int cols = grid.get(0).length();
      int count = 0;

      for (int i = 1; i < rows - 1; i++) {
         for (int j = 1; j < cols - 1; j++) {
            if (isXShape(grid, i, j)) {
               count++;
            }
         }
      }
      return count;
   }

   private static boolean isXShape(List<String> grid, int row, int col) {
      char center = grid.get(row).charAt(col);
      if (center != 'A') {
         return false;
      }

      char topLeft = grid.get(row - 1).charAt(col - 1);
      char topRight = grid.get(row - 1).charAt(col + 1);
      char bottomLeft = grid.get(row + 1).charAt(col - 1);
      char bottomRight = grid.get(row + 1).charAt(col + 1);

      return (topLeft == 'M' && bottomRight == 'S' || topLeft == 'S' && bottomRight == 'M') &&
            (topRight == 'M' && bottomLeft == 'S' || topRight == 'S' && bottomLeft == 'M');
   }
}
