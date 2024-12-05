package aoc.year2024;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Day05
{
   private final List<String> input;
   Map<Integer, List<Integer>> rulesMap = new HashMap<>();
   private final List<List<Integer>> listOfUpdates = new ArrayList<>();

   public Day05(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }

      makeTheMapRuleAndListOfUpdates(input);

      int sumOfMiddleElementsOfCorrectUpdates = calculateSumOfMiddleElementsOfCorrectUpdates();

      System.out.println("Part 1: " + sumOfMiddleElementsOfCorrectUpdates);

      int sumOfMiddleElementsOfRefinedBadUpdates = calculateSumOfMiddleElementsOfRefinedBadUpdates();
      System.out.println("Part 2: " + sumOfMiddleElementsOfRefinedBadUpdates);
   }



   private void makeTheMapRuleAndListOfUpdates(List<String> input)
   {
      boolean isSecondPart = false;

      for (String line : input)
      {
         if (line.trim().isEmpty())
         {
            // Blank line indicates the transition between map and list of lists
            isSecondPart = true;
            continue;
         }

         if (!isSecondPart)
         {
            // Parsing the first part into a map
            String[] parts = line.split("\\|");
            int key = Integer.parseInt(parts[0].trim());
            int value = Integer.parseInt(parts[1].trim());

            // Add the value to the list of values for the key
            rulesMap.putIfAbsent(key, new ArrayList<>());
            rulesMap.get(key).add(value);
         }
         else
         {
            // Parsing the second part into a list of lists
            String[] parts = line.split(",");
            List<Integer> numbers = new ArrayList<>();
            for (String part : parts)
            {
               numbers.add(Integer.parseInt(part.trim()));
            }
            listOfUpdates.add(numbers);
         }
      }
   }

   private int calculateSumOfMiddleElementsOfCorrectUpdates()
   {
      int sum = 0;

      for(List<Integer> updateList : listOfUpdates)
      {
          if(followsTheRules(updateList))
          {
             sum += getTheMiddleElement(updateList);
          }
      }

      return sum;
   }

   private int calculateSumOfMiddleElementsOfRefinedBadUpdates()
   {
      int sum = 0;

      for(List<Integer> updateList : listOfUpdates)
      {
         if(!followsTheRules(updateList))
         {
            updateList = reorderList(updateList);
            sum += getTheMiddleElement(updateList);
         }
      }

      return sum;
   }


   private boolean followsTheRules(List<Integer> updateList)
   {
      for (int index = 0; index < updateList.size(); index++) {
         Integer number = updateList.get(index);
         if (rulesMap.containsKey(number)) {
            List<Integer> currentKeyRules = rulesMap.get(number);
            for(Integer currentKeyRule : currentKeyRules)
            {
               int ruleIndex = updateList.indexOf(currentKeyRule);
               // If the rule exists in the list and appears before the current number
               if (ruleIndex != -1 && ruleIndex < index) {
                  return false;
               }
            }

         }
      }
      return true;
   }

   private int getTheMiddleElement(List<Integer> numbers)
   {
      if (numbers == null || numbers.isEmpty()) {
         throw new IllegalArgumentException("The list cannot be null or empty");
      }
      int middleIndex = numbers.size() / 2;
      return numbers.get(middleIndex);
   }

   private List<Integer> reorderList(List<Integer> updateList) {
      List<Integer> result = new ArrayList<>(updateList);
      boolean reordered;

      do {
         reordered = false;
         for (int index = 0; index < result.size(); index++) {
            Integer number = result.get(index);
            List<Integer> currentKeyRules = rulesMap.get(number);
            for (Integer currentKeyRule : currentKeyRules) {
               int ruleIndex = result.indexOf(currentKeyRule);
               // If the rule exists in the list and appears before the current number, swap them
               if (ruleIndex != -1 && ruleIndex > index) {
                  // Move `currentKeyRule` to appear before `number`
                  result.remove(ruleIndex);
                  result.add(index, currentKeyRule);
                  reordered = true;
                  break; // Reevaluate from the beginning after changes
               }
            }
            if (reordered) break;
         }
      } while (reordered);

      return result;
   }
}
