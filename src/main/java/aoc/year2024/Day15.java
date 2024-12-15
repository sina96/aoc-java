package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Day15
{
   private final List<String> input;
   enum Direction {
      UP, DOWN, LEFT, RIGHT
   }

   public Day15(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      solveTheFirstPart();
      solveTheSecondPart();
   }

   /**
    * Solves the first part of the puzzle:
    * <ul>
    * <li>Parses the initial grid and directions.</li>
    * <li>Moves the robot according to the directions, pushing single boxes if necessary.</li>
    * <li>Calculates the sum of box GPS coordinates (a custom metric) and prints the result.</li>
    * </ul>
    */
   private void solveTheFirstPart()
   {
      char[][] grid = parseGrid(input);
      ArrayList<Direction> directions = parseDirections(input);

      final int[] robotPosition = getRobotPosition(grid);

      if(robotPosition != null)
      {
         moveTheRobot(grid, robotPosition[0], robotPosition[1], directions, false);
      }

      long sumOfBoxGPS = calculateSumOfBoxGPS(grid);
      System.out.println("Part 1: " + sumOfBoxGPS);
   }

   /**
    * Solves the second part of the puzzle:
    * <ul>
    * <li>Parses the initial grid and directions.</li>
    * <li>Transforms the grid for part two (boxes are represented as pairs of '[' and ']').</li>
    * <li>Moves the robot according to the directions, pushing stacked boxes if necessary.</li>
    * <li>Calculates the sum of box GPS coordinates and prints the result.</li>
    * </ul>
    */
   private void solveTheSecondPart()
   {
      char[][] grid = parseGrid(input);
      ArrayList<Direction> directions = parseDirections(input);

      grid = makePartTwoGrid(grid);

      final int[] robotPosition = getRobotPosition(grid);

      if(robotPosition != null)
      {
         moveTheRobot(grid, robotPosition[0], robotPosition[1], directions, true);
      }

      long sumOfBoxGPS = calculateSumOfBoxGPS(grid);
      System.out.println("Part 2: " + sumOfBoxGPS);
   }

   /**
    * Moves the robot on the grid following the given directions.
    * If {@code isPartTwo} is true, different pushing rules apply (stacked boxes).
    *
    * @param grid the 2D grid representing the puzzle's state
    * @param y the robot's initial vertical position
    * @param x the robot's initial horizontal position
    * @param directions the list of directions to move the robot
    * @param isPartTwo true if we should use part two's pushing mechanics; false otherwise
    */
   private void moveTheRobot(char[][] grid, int y, int x, ArrayList<Direction> directions, boolean isPartTwo)
   {
      for(Direction direction : directions)
      {
         switch (direction)
         {
            case UP ->
                  y = (isPartTwo ? canMoveUpPart2(x, y, grid) : canMoveUp(x, y, grid)) ? y - 1 : y;
            case DOWN ->
                  y = (isPartTwo ? canMoveDownPart2(x, y, grid) : canMoveDown(x, y, grid)) ? y + 1 : y;
            case LEFT ->
                  x = (isPartTwo ? canMoveLeftPart2(x, y, grid) : canMoveLeft(x, y, grid)) ? x - 1 : x;
            case RIGHT ->
                  x = (isPartTwo ? canMoveRightPart2(x, y, grid) : canMoveRight(x, y, grid)) ? x + 1 : x;
         }
      }
   }

   /**
    * Calculates a custom metric known as "Box GPS" from the grid.
    * Each box is scored as (100 * rowIndex + columnIndex), and the total is summed.
    *
    * @param grid the 2D grid representing the puzzle's state
    * @return the sum of all box GPS values
    */
   private long calculateSumOfBoxGPS(char[][] grid)
   {
      long total = 0;
      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[y].length; x++) {
            if (grid[y][x] == 'O' || grid[y][x] == '[') {
               total += 100L * y + x;
            }
         }
      }
      return total;
   }

   /**
    * Checks if the robot can move down one cell in part 1 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move down; false otherwise
    */
   private boolean canMoveDown(final int x, final int y, final char[][] grid) {
      return attemptMovePartOne(x, y, 0, 1, grid);
   }

   /**
    * Checks if the robot can move up one cell in part 1 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move up; false otherwise
    */
   private boolean canMoveUp(final int x, final int y, final char[][] grid) {
      return attemptMovePartOne(x, y, 0, -1, grid);
   }

   /**
    * Checks if the robot can move right one cell in part 1 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move right; false otherwise
    */
   private boolean canMoveRight(final int x, final int y, final char[][] grid) {
      return attemptMovePartOne(x, y, 1, 0, grid);
   }

   /**
    * Checks if the robot can move left one cell in part 1 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move left; false otherwise
    */
   private boolean canMoveLeft(final int x, final int y, final char[][] grid) {
      return attemptMovePartOne(x, y, -1, 0, grid);
   }

   /**
    * Attempts a robot move for part 1 rules, including pushing single boxes if necessary.
    *
    * @param x current robot column
    * @param y current robot row
    * @param dx horizontal direction of move (-1, 0, or 1)
    * @param dy vertical direction of move (-1, 0, or 1)
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the move is successful; false otherwise
    */
   private boolean attemptMovePartOne(final int x, final int y, final int dx, final int dy, final char[][] grid) {
      int newX = x + dx;
      int newY = y + dy;

      if (!isInBounds(newX, newY, grid)) {
         return false;
      }

      char targetCell = grid[newY][newX];

      if (targetCell == '.') {
         // Move into empty space
         grid[y][x] = '.';
         grid[newY][newX] = '@';
         return true;
      } else if (targetCell == 'O') {
         // Attempt to push the box forward
         return attemptPushPartOne(x, y, dx, dy, grid);
      }

      return false;
   }

   /**
    * Attempts to push a chain of single boxes ('O') for part 1 rules.
    * Boxes are pushed in a line until an empty cell is found or a wall is encountered.
    *
    * @param x current robot column
    * @param y current robot row
    * @param dx horizontal direction of push
    * @param dy vertical direction of push
    * @param grid the 2D grid representing the puzzle's state
    * @return true if all boxes can be pushed and the move is completed; false otherwise
    */
   private boolean attemptPushPartOne(final int x, final int y, final int dx, final int dy, final char[][] grid)
   {
      int destinationX = x + 2 * dx; // The cell after the initial 'O'
      int destinationY = y + 2 * dy;
      boolean foundEmptySpot = false;

      // Search until we find an empty spot or hit a wall
      while (isInBounds(destinationX, destinationY, grid))
      {
         if (grid[destinationY][destinationX] == '.')
         {
            foundEmptySpot = true;
            break;
         }
         else if (grid[destinationY][destinationX] == '#')
         {
            // Hit a wall; can't push further
            break;
         }
         else
         {
            // Another 'O', continue looking further along the line
            destinationX += dx;
            destinationY += dy;
         }
      }

      if (!foundEmptySpot)
      {
         return false;
      }

      // Move all 'O' cells along the path to the destination
      int currentX = x + 2 * dx;
      int currentY = y + 2 * dy;
      while (true)
      {
         grid[currentY][currentX] = 'O';
         if (currentX == destinationX && currentY == destinationY)
         {
            break;
         }
         currentX += dx;
         currentY += dy;
      }

      // Move player into the box's old position
      grid[y][x] = '.';
      grid[y + dy][x + dx] = '@';

      return true;
   }

   /**
    * Checks if the robot can move down one cell in part 2 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move down; false otherwise
    */
   private boolean canMoveDownPart2(final int x, final int y, final char[][] grid) {
      return attemptMovePartTwo(x, y, 0, 1, grid);
   }

   /**
    * Checks if the robot can move up one cell in part 2 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move up; false otherwise
    */
   private boolean canMoveUpPart2(final int x, final int y, final char[][] grid) {
      return attemptMovePartTwo(x, y, 0, -1, grid);
   }

   /**
    * Checks if the robot can move right one cell in part 2 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move right; false otherwise
    */
   private boolean canMoveRightPart2(final int x, final int y, final char[][] grid) {
      return attemptMovePartTwo(x, y, 1, 0, grid);
   }

   /**
    * Checks if the robot can move left one cell in part 2 rules.
    *
    * @param x current robot column
    * @param y current robot row
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the robot can move left; false otherwise
    */
   private boolean canMoveLeftPart2(final int x, final int y, final char[][] grid) {
      return attemptMovePartTwo(x, y, -1, 0, grid);
   }

   /**
    * Attempts a robot move for part 2 rules, including pushing stacked boxes ('[' and ']') if necessary.
    *
    * @param x current robot column
    * @param y current robot row
    * @param dx horizontal direction of move
    * @param dy vertical direction of move
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the move is successful; false otherwise
    */
   private boolean attemptMovePartTwo(final int x, final int y, final int dx, final int dy, final char[][] grid) {
      int newX = x + dx;
      int newY = y + dy;

      if (!isInBounds(newX, newY, grid)) {
         return false;
      }

      char targetCell = grid[newY][newX];

      if (targetCell == '.') {
         // Move into empty space
         grid[y][x] = '.';
         grid[newY][newX] = '@';
         return true;
      } else if (targetCell == '[' || targetCell == ']') {
         // We need to push boxes (stacked)
         if (dx == 0) {
            // Vertical push
            return attemptVerticalPush(x, y, dx, dy, grid);
         } else if (dy == 0) {
            // Horizontal push
            return attemptHorizontalPush(x, y, dx, dy, grid);
         }
      }

      return false;
   }

   /**
    * Attempts to push stacked boxes vertically in part 2 rules.
    * Finds chains of boxes in a column and pushes them upwards or downwards if possible.
    *
    * @param x current robot column
    * @param y current robot row
    * @param dx horizontal direction of push (0 for vertical pushes)
    * @param dy vertical direction of push (-1 for up, 1 for down)
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the vertical push is successful; false otherwise
    */
   private boolean attemptVerticalPush(final int x, final int y, final int dx, final int dy, final char[][] grid) {
      final boolean pushingUp = (dy < 0);
      final Map<Integer, List<int[][]>> involvedBoxes = findInvolvedBoxes(grid, x, y, pushingUp);

      // Determine the extreme Y (topmost if pushing up, bottommost if pushing down)
      int extremeY = pushingUp
            ? involvedBoxes.keySet().stream().mapToInt(k -> k).min().getAsInt()
            : involvedBoxes.keySet().stream().mapToInt(k -> k).max().getAsInt();

      int nextY = extremeY + dy; // The row we want to push into

      if (!isInBounds(x, nextY, grid)) {
         return false;
      }

      // Check if space in the pushing direction is free (no walls)
      for (int[][] box : involvedBoxes.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
         int boxY1 = box[0][0], boxX1 = box[0][1];
         int boxY2 = box[1][0], boxX2 = box[1][1];
         if (grid[boxY1 + dy][boxX1] == '#' || grid[boxY2 + dy][boxX2] == '#') {
            return false;
         }
      }

      // Move the boxes
      if (pushingUp) {
         // Move boxes upward from the topmost row up to the robot's row
         for (int currentY = extremeY; currentY < y; currentY++) {
            final List<int[][]> boxesToMove = involvedBoxes.get(currentY);
            for (int[][] box : boxesToMove) {
               grid[currentY - 1][box[0][1]] = '[';
               grid[currentY - 1][box[1][1]] = ']';
               grid[currentY][box[0][1]] = '.';
               grid[currentY][box[1][1]] = '.';
            }
         }
      } else {
         // Move boxes downward from the bottommost row down to the robot's row
         for (int currentY = extremeY; currentY > y; currentY--) {
            final List<int[][]> boxesToMove = involvedBoxes.get(currentY);
            for (int[][] box : boxesToMove) {
               grid[currentY + 1][box[0][1]] = '[';
               grid[currentY + 1][box[1][1]] = ']';
               grid[currentY][box[0][1]] = '.';
               grid[currentY][box[1][1]] = '.';
            }
         }
      }

      // Move player into the space formerly occupied by a box
      grid[y + dy][x + dx] = '@';
      grid[y][x] = '.';
      return true;
   }

   /**
    * Attempts to push horizontally placed boxes in part 2 rules.
    * Boxes represented by '[' and ']' are pushed left or right until an empty space is found.
    *
    * @param x current robot column
    * @param y current robot row
    * @param dx horizontal direction of push (1 for right, -1 for left)
    * @param dy vertical direction of push (0 for horizontal pushes)
    * @param grid the 2D grid representing the puzzle's state
    * @return true if the horizontal push is successful; false otherwise
    */
   private boolean attemptHorizontalPush(final int x, final int y, final int dx, final int dy, final char[][] grid) {
      int directionX = dx > 0 ? 1 : -1;
      // Starting from the box next to the robot, find an empty spot to push the entire chain of boxes
      int destinationX = x + (3 * directionX);
      boolean foundEmptySpot = false;

      while (destinationX >= 0 && destinationX < grid[y].length) {
         if (grid[y][destinationX] == '.') {
            foundEmptySpot = true;
            break;
         } else if (grid[y][destinationX] == '#') {
            break;
         } else {
            destinationX += directionX;
         }
      }

      if (!foundEmptySpot) {
         return false;
      }

      // Move all boxes toward the destination
      if (dx > 0) {
         // Moving right
         for (int toMoveX = x + 2; toMoveX <= destinationX; toMoveX += 2) {
            grid[y][toMoveX] = '[';
            grid[y][toMoveX + 1] = ']';
         }
      } else {
         // Moving left
         for (int toMoveX = x - 2; toMoveX >= destinationX; toMoveX -= 2) {
            grid[y][toMoveX] = ']';
            grid[y][toMoveX - 1] = '[';
         }
      }

      // Move player into the position previously occupied by the box
      grid[y][x + dx] = '@';
      grid[y][x] = '.';
      return true;
   }

   /**
    * Finds all boxes involved in a vertical push in part 2 rules.
    * This method locates stacked boxes in a column above or below the starting position.
    *
    * @param grid the 2D grid representing the puzzle's state
    * @param x the robot's column position
    * @param y the robot's row position
    * @param up true if pushing upwards; false if pushing downwards
    * @return a map of row positions to lists of box coordinate pairs that are involved in the push
    */
   private Map<Integer, List<int[][]>> findInvolvedBoxes(char[][] grid, int x, int y, boolean up)
   {
      final Map<Integer, List<int[][]>> involvedBoxes = new HashMap<>();
      y = up ? y - 1 : y + 1;
      final int[][] start = grid[y][x] == '[' ? new int[][]{{y, x},{y, x+1}}
            : new int[][]{{y, x-1},{y, x}};
      final List<int[][]> boxes = new ArrayList<>();
      boxes.add(start);
      involvedBoxes.put(y, boxes);
      while (true) {
         final List<int[][]> boxesToAdd = new ArrayList<>();
         if (y > 0 && y < grid.length - 1) {
            final int nextY = up ? y - 1 : y + 1;
            for (final int[][] box : involvedBoxes.getOrDefault(y, new ArrayList<>())) {
               if (grid[nextY][box[0][1]] == '[') {
                  boxesToAdd.add(new int[][]{{nextY, box[0][1]},{nextY, box[1][1]}});
               }
               if (grid[nextY][box[0][1]] == ']') {
                  boxesToAdd.add(new int[][]{{nextY, box[0][1] - 1},{nextY, box[0][1]}});
               }
               if (grid[nextY][box[1][1]] == '[') {
                  boxesToAdd.add(new int[][]{{nextY, box[1][1]},{nextY, box[1][1] + 1}});
               }
            }
            if (boxesToAdd.isEmpty()) {
               break;
            } else {
               involvedBoxes.put(nextY, boxesToAdd);
               y = up ? y - 1 : y + 1;
            }
         } else {
            break;
         }
      }
      return involvedBoxes;
   }

   /**
    * Finds the current position of the robot in the grid.
    *
    * @param grid the 2D grid representing the puzzle's state
    * @return an array of two integers {y, x} indicating the robot's position, or null if not found
    */
   private int[] getRobotPosition(char[][] grid)
   {
      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[y].length; x++) {
            if (grid[y][x] == '@') {
               return new int[]{y, x};
            }
         }
      }
      return null;
   }

   /**
    * Parses the grid portion of the input. The grid is defined as consecutive non-blank lines
    * at the start of the input.
    *
    * @param input the list of input lines
    * @return a 2D character array representing the parsed grid
    */
   private char[][] parseGrid(List<String> input)
   {
      int size = 0;
      while (size < input.size())
      {
         if (input.get(size).isBlank()) {
            break;
         }
         size++;
      }
      final char[][] grid = new char[size][input.get(0).length()];
      for (int y = 0; y < size; y++) {
         final String line = input.get(y);
         for (int x = 0; x < line.length(); x++) {
            grid[y][x] = line.charAt(x);
         }
      }
      return grid;
   }

   /**
    * Parses the directions portion of the input. Directions appear after a blank line in the input.
    * Directions are represented by characters '<', '>', '^', and 'v'.
    *
    * @param input the list of input lines
    * @return an ArrayList of Direction enumerations representing the parsed directions
    */
   private ArrayList<Direction> parseDirections(final List<String> input) {
      ArrayList<Direction> directions = new ArrayList<>();
      boolean start = false;
      for (final String line : input) {
         if (start) {
            for (char c : line.toCharArray()) {
               switch (c)
               {
                  case '<' -> directions.add(Direction.LEFT);
                  case '>' -> directions.add(Direction.RIGHT);
                  case '^' -> directions.add(Direction.UP);
                  case 'v' -> directions.add(Direction.DOWN);
               }
            }
         } else if (line.isBlank()) {
            start = true;
         }
      }
      return directions;
   }

   /**
    * Transforms the part one grid into a part two grid. In part two, single boxes ('O')
    * are replaced by pairs of '[' and ']' to represent stacked boxes.
    * The grid width effectively doubles in part two.
    *
    * @param grid the original part one grid
    * @return a new 2D character array representing the transformed part two grid
    */
   private char[][] makePartTwoGrid(char[][] grid)
   {
      final char[][] newGrid = new char[grid.length][grid[0].length * 2];

      for (int y = 0; y < grid.length; y++) {
         for (int x = 0; x < grid[y].length; x++) {
            switch (grid[y][x]) {
               case 'O':
                  newGrid[y][2 * x] = '[';
                  newGrid[y][2 * x + 1] = ']';
                  break;
               case '@':
                  newGrid[y][2 * x] = '@';
                  newGrid[y][2 * x + 1] = '.';
                  break;
               default:
                  newGrid[y][2 * x] = grid[y][x];
                  newGrid[y][2 * x + 1] = grid[y][x];
            }
         }
      }
      return newGrid;
   }

   /**
    * Checks if the given (x, y) coordinates are within the bounds of the grid.
    *
    * @param x the column index to check
    * @param y the row index to check
    * @param grid the 2D grid representing the puzzle's state
    * @return true if (x, y) is within the grid bounds; false otherwise
    */
   private boolean isInBounds(final int x, final int y, final char[][] grid) {
      return x >= 0 && y >= 0 && y < grid.length && x < grid[y].length;
   }
}

