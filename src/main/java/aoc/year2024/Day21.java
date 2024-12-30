package aoc.year2024;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Day21
{
   private final List<String> input;

   private static final char START_CHAR = 'A';
   private static final char EMPTY_SPACE = ' ';
   private static final char[][] numPad = {
         { '7', '8', '9' },
         { '4', '5', '6' },
         { '1', '2', '3' },
         { ' ', '0', 'A' }
   };

   private static final char[][] dirPad = {
         { ' ', '^', 'A' },
         { '<', 'v', '>' }
   };

   public Day21(List<String> input)
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
      final Map<Character, Integer[]> numPadPositions = getPositionMap(numPad);
      final Map<Character, Integer[]> dirPadPositions = getPositionMap(dirPad);
      final Map<Move, String> numPadMoves = generateNumPadMoves(numPadPositions);
      final Map<Move, String> dirPadMoves = generateDirectionalPadMoves(dirPadPositions);
      long total = 0;
      final Map<Key, Long> moveToLength = new HashMap<>();
      for (final String code : input) {
         final long nr = getNumber(code);
         total += nr * getNrChars(code, numPadMoves, dirPadMoves, moveToLength, 2);
      }

      System.out.println("Part1: " + total);
   }

   private void solvePartTwo()
   {
      final Map<Character, Integer[]> numPadPositions = getPositionMap(numPad);
      final Map<Character, Integer[]> dirPadPositions = getPositionMap(dirPad);
      final Map<Move, String> numPadMoves = generateNumPadMoves(numPadPositions);
      final Map<Move, String> dirPadMoves = generateDirectionalPadMoves(dirPadPositions);
      long total = 0;
      final Map<Key, Long> moveToLength = new HashMap<>();
      for (final String code : input) {
         final long nr = getNumber(code);
         total += nr * getNrChars(code, numPadMoves, dirPadMoves, moveToLength, 25);
      }

      System.out.println("Part2: " + total);
   }


   private Map<Move, String> generateNumPadMoves(Map<Character, Integer[]> positionMap)
   {
      final Map<Move, String> moves = new HashMap<>();

      for (final Map.Entry<Character, Integer[]> startEntry : positionMap.entrySet()) {
         for (final Map.Entry<Character, Integer[]> endEntry : positionMap.entrySet()) {
            if (startEntry.getKey() != EMPTY_SPACE && endEntry.getKey() != EMPTY_SPACE) {
               moves.put(new Move(startEntry.getKey(), endEntry.getKey()),
                     generatePadMove(startEntry, endEntry, true));
            }
         }
      }

      return moves;
   }

   private Map<Move, String> generateDirectionalPadMoves(Map<Character, Integer[]> positionMap)
   {
      final Map<Move, String> moves = new HashMap<>();
      for (final Map.Entry<Character, Integer[]> startEntry : positionMap.entrySet()) {
         for (final Map.Entry<Character, Integer[]> endEntry : positionMap.entrySet()) {
            if (startEntry.getKey() != EMPTY_SPACE && endEntry.getKey() != EMPTY_SPACE) {
               moves.put(new Move(startEntry.getKey(), endEntry.getKey()),
                     generatePadMove(startEntry, endEntry, false));
            }
         }
      }

      return moves;
   }

   private String generatePadMove(Map.Entry<Character, Integer[]> startEntry, Map.Entry<Character, Integer[]> endEntry, boolean numPadMove)
   {
      final StringBuilder sb = new StringBuilder();
      final Integer[] start = startEntry.getValue();
      final Integer[] end = endEntry.getValue();
      final int diffX = end[0] - start[0];
      final int diffY = end[1] - start[1];

      if (numPadMove && start[1] == numPad.length - 1 && end[0] == 0) {
         // Bottom row, going to left, vertical first to avoid empty space
         sb.append(getVerticalMovement(diffY));
         sb.append(getHorizontalMovement(diffX));
         return sb.toString();
      } else if (numPadMove && start[0] == 0 && end[1] == numPad.length - 1) {
         // Left column, going to bottom row, right first to avoid empty space.
         sb.append(getHorizontalMovement(diffX));
         sb.append(getVerticalMovement(diffY));
         return sb.toString();
      } else if (!numPadMove && start[0] == 0) {
         // Directional pad, starting on <, go horizontal first to avoid empty space.
         sb.append(getHorizontalMovement(diffX));
         sb.append(getVerticalMovement(diffY));
         return sb.toString();
      } else if (!numPadMove && end[0] == 0) {
         // Directional pad, ending on <, go vertical first to avoid empty space.
         sb.append(getVerticalMovement(diffY));
         sb.append(getHorizontalMovement(diffX));
         return sb.toString();
      }

      if (diffY < 0 && diffX < 0) {
         // Going up/left, horizontal first.
         sb.append(getHorizontalMovement(diffX));
         sb.append(getVerticalMovement(diffY));
         return sb.toString();
      } else if (diffY > 0 && diffX < 0) {
         // Going down/left, horizontal first.
         sb.append(getHorizontalMovement(diffX));
         sb.append(getVerticalMovement(diffY));
         return sb.toString();
      } else if (diffY > 0 && diffX > 0) {
         // Going down/right, vertical first.
         sb.append(getVerticalMovement(diffY));
         sb.append(getHorizontalMovement(diffX));
         return sb.toString();
      } else if (diffY < 0 && diffX > 0) {
         // Going up/right, vertical first
         sb.append(getVerticalMovement(diffY));
         sb.append(getHorizontalMovement(diffX));
         return sb.toString();
      } else {
         // If all previous conditions do not apply, one of diffX/diffY == 0, so order
         // does not matter.
         sb.append(getHorizontalMovement(diffX));
         sb.append(getVerticalMovement(diffY));
         return sb.toString();
      }
   }

   private String getHorizontalMovement(int diffX)
   {
      StringBuilder s = new StringBuilder();
      if (diffX > 0) {
         s.append(">".repeat(diffX));
      } else if (diffX < 0) {
         s.append("<".repeat(Math.max(0, -diffX)));
      }
      return s.toString();
   }

   private String getVerticalMovement(int diffY)
   {
      StringBuilder s = new StringBuilder();
      if (diffY > 0) {
         s.append("v".repeat(diffY));
      } else if (diffY < 0) {
         s.append("^".repeat(Math.max(0, -diffY)));
      }
      return s.toString();
   }

   private long getNrChars(final String code, final Map<Move, String> numPadMoves, final Map<Move, String> dirPadMoves,
         final Map<Key, Long> memoMap, final int depth) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < code.length(); i++) {
         final Move move = new Move(i == 0 ? START_CHAR : code.charAt(i - 1), code.charAt(i));
         sb.append(numPadMoves.get(move));
         sb.append("A");
      }
      final String start = sb.toString();

      return countChars(start, depth, dirPadMoves, memoMap);
   }

   private long countChars(final String code, final int depth, final Map<Move, String> dirPadMoves,
         final Map<Key, Long> memoMap) {
      if (depth == 0) {
         return code.length();
      }
      if (code.equals("A")) {
         return 1;
      }
      final Key key = new Key(code, depth);
      if (memoMap.containsKey(key)) {
         return memoMap.get(key);
      }

      long total = 0;
      for (final String move : code.split("A")) {
         // We process every move, which is defined by ending in an A
         final StringBuilder sb = new StringBuilder();
         for (int i = 0; i <= move.length(); i++) {
            final Move m = new Move(i == 0 ? START_CHAR : move.charAt(i - 1), i == move.length() ? START_CHAR : move.charAt(i));
            sb.append(dirPadMoves.get(m));
            sb.append("A");
         }
         total += countChars(sb.toString(), depth - 1, dirPadMoves, memoMap);
      }
      memoMap.put(key, total);
      return total;
   }

   private int getNumber(final String s) {
      return Integer.parseInt(s.substring(0, s.length() - 1));
   }

   private Map<Character, Integer[]> getPositionMap(char[][] pad)
   {
      final Map<Character, Integer[]> map = new HashMap<>();
      for (int y = 0; y < pad.length; y++) {
         for (int x = 0; x < pad[y].length; x++) {
            map.put(pad[y][x], new Integer[]{x, y});
         }
      }
      return map;
   }

   static final class Move
   {
      private final char start;
      private final char end;

      Move(char start, char end)
      {
         this.start = start;
         this.end = end;
      }

      public char start()
      {
         return start;
      }

      public char end()
      {
         return end;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
            return true;
         if (obj == null || obj.getClass() != this.getClass())
            return false;
         var that = (Move) obj;
         return this.start == that.start && this.end == that.end;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(start, end);
      }

      @Override
      public String toString()
      {
         return "Move[" + "start=" + start + ", " + "end=" + end + ']';
      }

      }

   static final class Key
   {
      private final String code;
      private final int depth;

      Key(String code, int depth)
      {
         this.code = code;
         this.depth = depth;
      }

      public String code()
      {
         return code;
      }

      public int depth()
      {
         return depth;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
            return true;
         if (obj == null || obj.getClass() != this.getClass())
            return false;
         var that = (Key) obj;
         return Objects.equals(this.code, that.code) && this.depth == that.depth;
      }

      @Override
      public int hashCode()
      {
         return Objects.hash(code, depth);
      }

      @Override
      public String toString()
      {
         return "Key[" + "code=" + code + ", " + "depth=" + depth + ']';
      }

      }
}

