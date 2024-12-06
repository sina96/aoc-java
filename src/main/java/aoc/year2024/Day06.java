package aoc.year2024;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Day06
{
   public static final char DOT = '.';
   private final List<String> input;
   private final Map<Character, Character> nextDirectionMap = new HashMap<>();
   private final List<Character> directions = List.of('^', '>', 'v', '<');
   private static final char VISITED = 'X';
   private static final char BLOCK = '#';
   private Character startCharacter;

   public Day06(List<String> input)
   {
      this.input = input;
      makeTheDirectionsMap(nextDirectionMap);
   }

   private void makeTheDirectionsMap(Map<Character, Character> directionsMap)
   {
      directionsMap.put('^', '>');
      directionsMap.put('>', 'v');
      directionsMap.put('v', '<');
      directionsMap.put('<', '^');
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      char[][] partOneGrid = makeTheGrid(input);
      char[][] partTwoGrid = makeTheGrid(input);


      int[] position = findGuardStartCharacterAndPosition(partOneGrid, directions);

      if(position == null)
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      
      int numOfXInGrid = countGuardMoves(partOneGrid, position, startCharacter);
      System.out.println("Part 1: " + numOfXInGrid);

      int numOfLoopsCreated = countPossibleLoopToBeMade(partTwoGrid, position, startCharacter);
      System.out.println("Part 2: " + numOfLoopsCreated);
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

   private int[] findGuardStartCharacterAndPosition(char[][] grid, List<Character> targets)
   {
      for(char target : targets)
      {
         for (int row = 0; row < grid.length; row++) { // Traverse rows
            for (int col = 0; col < grid[row].length; col++) { // Traverse columns
               if (grid[row][col] == target) { // Check if the current cell contains the target
                  startCharacter = target;
                  return new int[]{row, col}; // Return the position as an array [row, col]
               }
            }
         }
      }
      return null; // Return null if the character is not found
   }

   private int countGuardMoves(char[][] grid, int[] position, Character currentDirection) {
      int moveCount = 1; // Start counting the initial position as visited.

      while (true) {
         // Mark current position as visited.
         grid[position[0]][position[1]] = VISITED;

         // Determine the next position based on the current direction.
         int[] nextPosition = determineNextPosition(currentDirection, position);

         // Check if the next position is out of bounds.
         if (isOutOfBounds(grid, nextPosition)) {
            break; // Stop if we move out of bounds.
         }

         // Check the contents of the next position.
         char nextCell = grid[nextPosition[0]][nextPosition[1]];
         if (nextCell == BLOCK) {
            // Found a block; change direction.
            currentDirection = nextDirectionMap.get(currentDirection);
         } else {
            // Move to the next position.
            position = nextPosition;

            // Only count the move if the position hasn't been visited yet.
            if (grid[position[0]][position[1]] != VISITED) {
               moveCount++;
            }
         }
      }

      return moveCount;
   }


   public int countPossibleLoopToBeMade(char[][] grid, int[] startPosition, Character startDirection) {
      int numRows = grid.length;
      int numCols = grid[0].length;
      int loopCounter = 0;

      for (int i = 0; i < numRows; i++) {
         for (int j = 0; j < numCols; j++) {
            if (grid[i][j] == DOT) { // Check only empty cells
               grid[i][j] = BLOCK; // Temporarily place an obstacle
               if (doesCreateLoop(grid, startPosition, startDirection)) {
                  loopCounter++;
               }
               grid[i][j] = DOT; // Remove the obstacle
            }
         }
      }
      return loopCounter;
   }

   private boolean isOutOfBounds(char[][] grid, int[] position)
   {
      int numRows = grid.length;
      int numCols = grid[0].length;
      return position[0] < 0 || position[0] >= numRows || position[1] < 0 || position[1] >= numCols;
   }

   private int[] determineNextPosition(Character currentCharacter, int[] position) {
      switch (currentCharacter) {
         case '^': // Facing up
            return new int[]{position[0] - 1, position[1]};
         case 'v': // Facing down
            return new int[]{position[0] + 1, position[1]};
         case '>': // Facing right
            return new int[]{position[0], position[1] + 1};
         case '<': // Facing left
            return new int[]{position[0], position[1] - 1};
         default:
            throw new IllegalArgumentException("Invalid direction character: " + currentCharacter);
      }
   }

   private boolean doesCreateLoop(char[][] grid, int[] startPosition, Character startDirection) {
      Set<String> visitedStates = new HashSet<>();
      int[] position = startPosition.clone();
      Character currentCharacter = startDirection;

      while (true) {
         String state = position[0] + "," + position[1] + "," + currentCharacter;
         if (visitedStates.contains(state)) {
            return true; // Loop detected
         }
         visitedStates.add(state);

         int[] nextPosition = determineNextPosition(currentCharacter, position);

         if (isOutOfBounds(grid, nextPosition)) {
            break; // Out of bounds, no loop
         } else if (grid[nextPosition[0]][nextPosition[1]] == BLOCK) {
            // Found a block, change direction
            currentCharacter = nextDirectionMap.get(currentCharacter);
         } else {
            // Move to the next position
            position = nextPosition;
         }
      }
      return false;
   }

}
