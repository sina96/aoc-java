package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Day08
{
   public static final char DOT = '.';
   private final List<String> input;
   private final Map<Character, List<int[]>> antennaPositionMap = new HashMap<>();


   public Day08(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      char[][] grid = makeTheGrid(input);
      makeTheAntennaMap(grid);

      int nrOfUniquePossibleAntinodes = countPossibleUniqueAntinodes(grid, antennaPositionMap);

      System.out.println("Part 1: " + nrOfUniquePossibleAntinodes);

      int nrOfUniqueHarmonicAntinotes = countPossibleUniqueHarmonicAntinodes(grid, antennaPositionMap);
      System.out.println("Part 2: " +nrOfUniqueHarmonicAntinotes);
   }

   private char[][] makeTheGrid(List<String> gridData)
   {
      int rows = gridData.size();
      int cols = gridData.get(0).length();
      char[][] gridArray = new char[rows][cols];
      for (int i = 0; i < rows; i++) {
         gridArray[i] = gridData.get(i).toCharArray();
      }
      return gridArray;
   }

   private void makeTheAntennaMap(char[][] grid)
   {
      int numRows = grid.length;
      int numCols = grid[0].length;

      for (int i = 0; i < numRows; i++) {
         for (int j = 0; j < numCols; j++) {
            int[] position = new int[]{i, j};
            if (grid[i][j] != DOT) // Check non-empty cells
            {
               antennaPositionMap.putIfAbsent(grid[i][j], new ArrayList<>());
               antennaPositionMap.get(grid[i][j]).add(position);
            }
         }
      }
   }

   private int countPossibleUniqueAntinodes(char[][] grid, Map<Character, List<int[]>> posMap)
   {
      Set<String> antinodePositionSet = new HashSet<>();

      int numRows = grid.length;
      int numCols = grid[0].length;

      for(Map.Entry<Character, List<int[]>> entry : posMap.entrySet())
      {
         List<int[]> positions = entry.getValue();

         if(positions.size() < 2)
         {
            break;
         }

         for (int k = 0; k < positions.size(); k++) {
            for (int l = 0; l < positions.size(); l++)
            {
               int[] pos1 = positions.get(k);
               int[] pos2 = positions.get(l);
               if (k != l)
               {
                  int antinodeRow =  pos2[0] + (pos2[0] - pos1[0]);
                  int antinodeCol =  pos2[1] + (pos2[1] - pos1[1]);
                  if(isValidPosition(antinodeRow, antinodeCol, numRows, numCols))
                  {
                     antinodePositionSet.add(createPositionKey(antinodeRow, antinodeCol));
                  }
               }
            }
         }
      }

      return antinodePositionSet.size();
   }

   private int countPossibleUniqueHarmonicAntinodes(char[][] grid, Map<Character, List<int[]>> posMap)
   {
      Set<String> antinodePositionSet = new HashSet<>();

      int numRows = grid.length;
      int numCols = grid[0].length;

      for(Map.Entry<Character, List<int[]>> entry : posMap.entrySet())
      {
         List<int[]> positions = entry.getValue();

         if(positions.size() < 2)
         {
            break;
         }

         for (int k = 0; k < positions.size(); k++) {
            for (int l = 0; l < positions.size(); l++)
            {
               if (k != l)
               {
                  int[] pos1 = positions.get(k);
                  int[] pos2 = positions.get(l);
                  int dx = (pos2[0] - pos1[0]);
                  int dy = (pos2[1] - pos1[1]);

                  // Start with the second position
                  int currentRow = pos2[0];
                  int currentCol = pos2[1];
                  while (isValidPosition(currentRow, currentCol, numRows, numCols)) {
                     // Add the current position to the set
                     antinodePositionSet.add(createPositionKey(currentRow, currentCol));
                     // Move to the next position in the harmonic sequence
                     currentRow += dx;
                     currentCol += dy;
                  }
               }
            }
         }
      }

      return antinodePositionSet.size();
   }

   private boolean isValidPosition(int row, int col, int numRows, int numCols) {
      return row >= 0 && row < numRows && col >= 0 && col < numCols;
   }

   private String createPositionKey(int row, int col) {
      return row + "," + col;
   }
}
