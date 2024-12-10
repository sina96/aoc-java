package aoc.year2024;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;


public class Day09
{
   private final List<String> input;

   public Day09(List<String> input)
   {
      this.input = input;
   }

   public void solve()
   {
      if (input == null || input.isEmpty())
      {
         throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      String diskInput = input.get(0);

      solvePartOne(diskInput);
      solvePartTwo(diskInput);
   }

   private void solvePartOne(String diskInput)
   {
      final int[] sizeAndEmptyBlocks = getSizeAndEmptyBlocks(diskInput);
      final int[] blocks = buildBlocks(diskInput, sizeAndEmptyBlocks[0]);

      moveBlocks(sizeAndEmptyBlocks[1], blocks);
      final long checksum = calculateChecksum(blocks);
      System.out.println("Part 1: " + checksum);
   }

   private int[] getSizeAndEmptyBlocks(final String diskInput) {
      int size = 0;
      int nrEmptyBlocks = 0;

      for (int i = 0; i < diskInput.length(); i++) {
         int current = getIntValue(diskInput, i);
         size += current;
         if (i % 2 != 0) {
            nrEmptyBlocks += current;
         }
      }

      return new int[]{size, nrEmptyBlocks};
   }

   private int[] buildBlocks(final String diskInput, final int size) {
      final int[] blocks = new int[size];
      int processed = 0;
      int currentNumber = 0;

      for (int i = 0; i < diskInput.length(); i++) {
         final int current = getIntValue(diskInput, i);
         for (int j = 0; j < current; j++) {
            blocks[processed + j] = (i % 2 == 0) ? currentNumber : -1;
         }
         processed += current;
         if (i % 2 == 0) {
            currentNumber++;
         }
      }

      return blocks;
   }

   private void moveBlocks(int nrEmptyBlocks, int[] blocks) {
      int endIdx = blocks.length - 1; // Start at the last index of the array
      int nrProcessed = 0; // Counter for the number of empty blocks processed

      // Iterate through the blocks array
      for (int j = 0; j < endIdx && nrProcessed < nrEmptyBlocks; j++) {
         // Check if the current block is empty (-1)
         if (blocks[j] == -1) {
            // Find the last non-empty block by moving endIdx backward
            while (blocks[endIdx] == -1) {
               endIdx--; // Skip empty blocks from the end
            }

            // If endIdx has moved past the current index, stop the process
            if (endIdx <= j) {
               break;
            }

            // Move the last non-empty block to the current empty block position
            blocks[j] = blocks[endIdx];
            // Mark the last block's position as empty
            blocks[endIdx] = -1;

            // Update endIdx to point to the next non-empty block
            endIdx--;
            // Increment the counter for processed empty blocks
            nrProcessed++;
         }
      }
   }

   private void solvePartTwo(String inputDisk)
   {
      final int[] sizeAndEmptyBlocks = getSizeAndEmptyBlocks(inputDisk);
      final int[] blocks = buildBlocks(inputDisk, sizeAndEmptyBlocks[0]);
      moveFiles(blocks, getFileSizes(inputDisk), getEmptyBlockSizes(blocks), getFileLocations(blocks),
            getEmptySpaceLocations(blocks));

      final long checksum = calculateChecksum(blocks);
      System.out.println("Part 2: " + checksum);
   }

   private void moveFiles(final int[] blocks, final TreeMap<Integer, Integer> fileSizes,
         TreeMap<Integer, Integer> emptyBlockSizes, final TreeMap<Integer, Integer> fileLocations,
         TreeMap<Integer, Integer> emptySpaceLocations) {

      // Iterate through all files in descending order of their indices
      for (int fileIdx = fileSizes.firstKey(); fileIdx >= 0; fileIdx--) {
         int emptySpaceIdx = 0; // Start with the first empty block
         int currentFileSize = fileSizes.get(fileIdx); // Get the size of the current file
         boolean foundSpace = false; // Flag to indicate if suitable space is found

         // Find a suitable empty block that can fit the current file
         while (emptySpaceIdx < emptyBlockSizes.size()) {
            int currentEmptyBlockSize = emptyBlockSizes.get(emptySpaceIdx);
            if (currentFileSize <= currentEmptyBlockSize) {
               foundSpace = true; // Space found
               break;
            }
            emptySpaceIdx++; // Move to the next empty block
         }

         // If a suitable space is found, proceed to move the file
         if (foundSpace) {
            int fileLocation = fileLocations.get(fileIdx); // Current location of the file
            int emptySpaceLocation = emptySpaceLocations.get(emptySpaceIdx); // Location of the empty block

            // Only move the file if the empty space is earlier in the array than its current location
            if (emptySpaceLocation < fileLocation) {
               // Clear the file's current location by marking its blocks as empty (-1)
               for (int i = fileLocation; i < fileLocation + currentFileSize; i++) {
                  blocks[i] = -1;
               }

               // Move the file to the new empty block
               for (int i = emptySpaceLocation; i < emptySpaceLocation + currentFileSize; i++) {
                  blocks[i] = fileIdx;
               }

               // Update empty block sizes and locations to reflect the new state of the blocks array
               emptyBlockSizes = getEmptyBlockSizes(blocks);
               emptySpaceLocations = getEmptySpaceLocations(blocks);
            }
         }
      }
   }


   private TreeMap<Integer, Integer> getFileSizes(final String diskInput)
   {
      final TreeMap<Integer, Integer> map = new TreeMap<>(Collections.reverseOrder());
      for (int i = 0, nr = 0; i < diskInput.length(); i += 2) {
         map.put(nr++, getIntValue(diskInput, i));
      }

      return map;
   }

   private TreeMap<Integer, Integer> getEmptyBlockSizes(final int[] blocks) {
      TreeMap<Integer, Integer> map = new TreeMap<>();
      int nr = 0;

      for (int i = 0; i < blocks.length; i++) {
         if (blocks[i] == -1) {
            int size = 0;
            while (i < blocks.length && blocks[i] == -1) {
               size++;
               i++;
            }
            map.put(nr++, size);
         }
      }

      return map;
   }

   private TreeMap<Integer, Integer> getFileLocations(final int[] blocks) {
      TreeMap<Integer, Integer> map = new TreeMap<>(Collections.reverseOrder());
      int nr = 0;

      for (int i = 0; i < blocks.length; i++) {
         if (blocks[i] != -1) {
            map.put(nr++, i);
            int current = blocks[i];
            while (i < blocks.length && blocks[i] == current) {
               i++;
            }
            i--; // Adjust to account for the extra increment in the while loop
         }
      }

      return map;
   }

   private TreeMap<Integer, Integer> getEmptySpaceLocations(final int[] blocks) {
      TreeMap<Integer, Integer> map = new TreeMap<>();
      int nr = 0;

      for (int i = 0; i < blocks.length; i++) {
         if (blocks[i] == -1) {
            map.put(nr++, i);
            while (i < blocks.length && blocks[i] == -1) {
               i++;
            }
            i--; // Adjust to account for the extra increment in the while loop
         }
      }

      return map;
   }

   private long calculateChecksum(final int[] blocks) {
      long checksum = 0;
      for (int i = 0; i < blocks.length; i++) {
         if (blocks[i] != -1) {
            checksum += (long) blocks[i] * i;
         }
      }
      return checksum;
   }

   private static int getIntValue(String diskInput, int i)
   {
      return diskInput.charAt(i) - '0';
   }
}
