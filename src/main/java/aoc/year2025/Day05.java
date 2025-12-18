package aoc.year2025;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Day05
{
   private final List<String> input;

   public Day05(List<String> input) {
      this.input = input;
   }

   public void solve() {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      List<Range> mergedRanges = parseAndMergeRanges(input);
      List<Long> allIds = populateAllIds(input);

      long partOneSolution = countAllFreshIds(allIds, mergedRanges);
      System.out.println("Part 1: " + partOneSolution);

      long partTwoSolution = totalCoveredSize(mergedRanges);

      System.out.println("Part 2: " + partTwoSolution);
   }

   private List<Range> parseAndMergeRanges(List<String> input)
   {
      List<Range> ranges = getRanges(input);

      if (ranges.isEmpty())
         return ranges;

      ranges.sort(Comparator.comparingLong(r -> r.start));

      List<Range> merged = new ArrayList<>();
      Range cur = ranges.get(0);

      for (int i = 1; i < ranges.size(); i++) {
         Range nxt = ranges.get(i);

         // merge if overlapping or adjacent
         if (nxt.start <= cur.end + 1) {
            cur = new Range(cur.start, Math.max(cur.end, nxt.end));
         } else {
            merged.add(cur);
            cur = nxt;
         }
      }
      merged.add(cur);
      return merged;
   }

   private List<Long> populateAllIds(List<String> input)
   {
      List<Long> allIds = new ArrayList<>();
      boolean parsingRanges = true;
      for (String line : input) {
         if (parsingRanges) {
            if (line.isBlank()) parsingRanges = false;
         } else {
            allIds.add(Long.parseLong(line.trim()));
         }
      }
      allIds.sort(Long::compare);
      return allIds;
   }

   private long countAllFreshIds(List<Long> allIds, List<Range> mergedRanges)
   {
      int currRangeIndex = 0;
      long count = 0;

      for (long id : allIds) {
         while (currRangeIndex < mergedRanges.size() && id > mergedRanges.get(currRangeIndex).end)
            currRangeIndex++;
         if (currRangeIndex == mergedRanges.size())
            break;
         Range currentRange = mergedRanges.get(currRangeIndex);
         if (id >= currentRange.start)
            count++;
      }
      return count;
   }

   static long totalCoveredSize(List<Range> ranges) {
      if (ranges.isEmpty()) return 0L;

      ranges.sort(Comparator.comparingLong(r -> r.start));

      long total = 0L;
      long curStart = ranges.get(0).start;
      long curEnd   = ranges.get(0).end;

      for (int i = 1; i < ranges.size(); i++) {
         Range r = ranges.get(i);

         // overlaps or touches (adjacent)
         if (r.start <= curEnd + 1) {
            curEnd = Math.max(curEnd, r.end);
         } else {
            total = safeAdd(total, safeLen(curStart, curEnd));
            curStart = r.start;
            curEnd = r.end;
         }
      }

      total = safeAdd(total, safeLen(curStart, curEnd));
      return total;
   }

   private static List<Range> getRanges(List<String> input)
   {
      List<Range> ranges = new ArrayList<>();
      boolean parsingRanges = true;

      for (String line : input) {
         if (parsingRanges) {
            if (line.isBlank()) {
               parsingRanges = false;
               continue;
            }
            String[] split = line.split("-", 2);
            long s = Long.parseLong(split[0].trim());
            long e = Long.parseLong(split[1].trim());
            ranges.add(new Range(s, e));
         } else {
            // ignore here; IDs handled elsewhere
         }
      }
      return ranges;
   }

   static long safeLen(long start, long end) {
      // inclusive length = end - start + 1, overflow-safe
      return Math.addExact(Math.subtractExact(end, start), 1L);
   }

   static long safeAdd(long a, long b) {
      return Math.addExact(a, b);
   }

   static final class Range
   {
      final long start;
      final long end;

      Range(long start, long end) {
         if (end < start)
            throw new IllegalArgumentException("Invalid range: " + start + "-" + end);

         this.start = start;
         this.end = end;
      }
   }
}

