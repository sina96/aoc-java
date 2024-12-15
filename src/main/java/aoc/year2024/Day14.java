package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Day14
{
   private final List<String> input;

   private static final int MAX_WIDTH = 101;
   private static final int MAX_HEIGHT = 103;

   HashMap<String, ArrayList<GridRobot>> robotPositionMap;

   public Day14(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      final List<GridRobot> gridRobotList = fillTheGridRobotList(input);

      solveAll(gridRobotList);
   }

   private List<GridRobot> fillTheGridRobotList(List<String> input)
   {
      final List<GridRobot> robots = new ArrayList<>();
      for (final String line : input) {
         final int x = Integer.parseInt(line.split("=")[1].split(",")[0]);
         final int y = Integer.parseInt(line.split("=")[1].split(",")[1].split(" ")[0]);
         final int velX = Integer.parseInt(line.split("v=")[1].split(",")[0]);
         final int velY = Integer.parseInt(line.split("v=")[1].split(",")[1]);
         robots.add(new GridRobot(x, y, velX, velY));
      }
      return robots;
   }

   private void solveAll(List<GridRobot> gridRobotList)
   {
      int seconds = 1;
      while(true)
      {
         robotPositionMap = new HashMap<>();

         for (GridRobot robot : gridRobotList) {
            moveRobot(robot, true);
         }

         if(seconds == 100)
         {
            System.out.println("Safety factor after 100 seconds :" + getSafetyFactor(gridRobotList));
         }

         if (hasHorizontalLine(gridRobotList)) {
            break;
         }
         seconds++;
      }

      printTree();
      System.out.println("Seconds to make the christmas tree: " + seconds);
   }

   private void moveRobot(GridRobot gridRobot, boolean addToMap)
   {
      final int newX = gridRobot.getX() + gridRobot.getVelocityX();
      final int newY = gridRobot.getY() + gridRobot.getVelocityY();
      gridRobot.setX(newX >= Day14.MAX_WIDTH ? newX % Day14.MAX_WIDTH : newX < 0 ? Day14.MAX_WIDTH + newX : newX);
      gridRobot.setY(newY >= Day14.MAX_HEIGHT ? newY % Day14.MAX_HEIGHT : newY < 0 ? Day14.MAX_HEIGHT + newY : newY);

      if(addToMap)
      {
         String mapPosKey = getMapPosKey(gridRobot.getX(), gridRobot.getY());
         ArrayList<GridRobot> robots = robotPositionMap.getOrDefault(mapPosKey, new ArrayList<>());
         robots.add(gridRobot);
         robotPositionMap.put(mapPosKey, robots);
      }
   }

   private long getSafetyFactor(final List<GridRobot> robots) {
      final int middleX = Day14.MAX_WIDTH / 2;
      final int middleY = Day14.MAX_HEIGHT / 2;
      final long q1 = robots.stream().filter(r -> r.getX() < middleX && r.getY() < middleY).count();
      final long q2 = robots.stream().filter(r -> r.getX() > middleX && r.getY() < middleY).count();
      final long q3 = robots.stream().filter(r -> r.getX() < middleX && r.getY() > middleY).count();
      final long q4 = robots.stream().filter(r -> r.getX() > middleX && r.getY() > middleY).count();
      return q1 * q2 * q3 * q4;
   }

   private boolean hasHorizontalLine(final List<GridRobot> robots)
   {
      // Checks if we have 10 unique robots next to each-other to make the base/frame for the tree
      final int lineSize = 10;
      for (final GridRobot robot : robots) {
         final int x = robot.getX();
         for (int i = 1; i <= lineSize; i++) {
            if (!robots.contains(new GridRobot(x + i, robot.getY(), 0, 0))) {
               break;
            } else if (i == lineSize) {
               return true;
            }
         }
      }
      return false;
   }

   private void printTree() {
      for (int i = 0;i < MAX_HEIGHT; i++) {
         for(int j = 0;j < MAX_WIDTH; j++) {
            if (robotPositionMap.containsKey(getMapPosKey(j, i))) {
               System.out.print("X");
            } else {
               System.out.print(".");
            }
         }
         System.out.println();
      }
   }

   private String getMapPosKey(int x, int y)
   {
      return x+"-"+y;
   }

   private static class GridRobot
   {
      int x;
      int y;
      int velX;
      int velY;

      public GridRobot(int x, int y, int velX, int velY)
      {
         this.x = x;
         this.y = y;
         this.velX = velX;
         this.velY = velY;
      }

      public int getVelocityX()
      {
         return velX;
      }

      public int getVelocityY()
      {
         return velY;
      }

      public int getY()
      {
         return y;
      }

      public void setY(int y)
      {
         this.y = y;
      }

      public int getX()
      {
         return x;
      }

      public void setX(int x)
      {
         this.x = x;
      }

      public void setPosition(int x, int y)
      {
         this.x = x;
         this.y = y;
      }

      @Override
      public boolean equals(Object o)
      {
         if (!(o instanceof GridRobot gridRobot))
            return false;
         return getX() == gridRobot.getX() && getY() == gridRobot.getY();
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(getX(), getY());
      }
   }
}

