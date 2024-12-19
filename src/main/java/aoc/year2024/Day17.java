package aoc.year2024;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;


public class Day17
{
   private final List<String> input;

   public Day17(List<String> input)
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

   private void solvePartTwo()
   {
      Set<Long> candidates = new HashSet<>();

      candidates.add(0L);
      Computer computer = getComputer(input);

      int i = 1;
      while (i <= computer.program.length)
      {
         final Set<Long> newCandidates = new HashSet<>();
         for (final long candidate : candidates) {
            newCandidates.addAll(findCandidates(candidate, i, input));
         }
         candidates = newCandidates;
         i++;
      }

      long lowestCandidate = candidates.stream().mapToLong(l -> l).min().getAsLong();

      System.out.println("Part 2: "+ lowestCandidate);
   }

   private Set<Long> findCandidates(final long start, final int position, final List<String> input)
   {
      final Set<Long> candidates = new HashSet<>();

      // Check a range of possible values for A starting from 0. in range of 8 (3bits)
      for (long a = start; a < start + 8; a++) {
         final Computer computer = getComputer(input);
         computer.setRegisterA(a);
         computer.run();
         final int[] program = computer.program;
         final long[] output = computer.getOutputs();
         boolean valid = true;

         // going backwards in program
         for (int i = position; i > 0; i--) {
            if (program[program.length - position] != output[output.length - position]) {
               valid = false;
               break;
            }
         }
         if (valid) {
            // Adjust the candidate value based on the position and add it to the set
            candidates.add(position < program.length ? a << 3 : a);
         }
      }
      return candidates;
   }

   private void solvePartOne()
   {
      Computer computer = getComputer(input);
      computer.run();

      String outputs = Arrays.toString(computer.getOutputs()).replace("[", "")  // Remove [
         .replace("]", "")  // Remove ]
         .replace(" ", "");;
      System.out.println("Part 1: " + outputs);
   }

   private Computer getComputer(List<String> input)
   {
      long registerAFromInput = Long.parseLong(input.get(0).split(": ")[1]);
      long registerBFromInput = Long.parseLong(input.get(1).split(": ")[1]);
      long registerCFromInput = Long.parseLong(input.get(2).split(": ")[1]);
      int[] programFromInput = Arrays.stream(input.get(4).split(": ")[1].split(","))
            .mapToInt(Integer::parseInt)
            .toArray();

      return new Computer(registerAFromInput, registerBFromInput, registerCFromInput, programFromInput);
   }

   static class Computer {
      long registerA;
      long registerB;
      long registerC;
      int[] program;
      int pointer = 0;
      long[] outputs = {};

      public Computer(long registerA, long registerB, long registerC, int[] program)
      {
         this.registerA = registerA;
         this.registerB = registerB;
         this.registerC = registerC;
         this.program = program;
      }

      public void run(){
         while(pointer < program.length)
            runOperation(program[pointer], program[pointer + 1]);
      }

      private void runOperation(int opcode, int operand)
      {
         switch (opcode)
         {
            case 0 -> adv(operand);
            case 1 -> bxl(operand);
            case 2 -> bst(operand);
            case 3 -> jnz(operand);
            case 4 -> bxc();
            case 5 -> out(operand);
            case 6 -> bdv(operand);
            case 7 -> cdv(operand);
            default -> throw new IllegalArgumentException("Unknown opcode: " + opcode);
         }
      }

      private long getComboValue(int operand)
      {
         return switch (operand)
         {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> registerA;
            case 5 -> registerB;
            case 6 -> registerC;
            case 7 -> throw new IllegalArgumentException("Reserved combo operand 7 found, program not valid");
            default -> throw new IllegalArgumentException("Unknown combo operand " + operand);
         };
      }

      private void adv(int operand)
      {
         divideAndSet(operand, 'A');
         pointer += 2;
      }

      private void cdv(int operand)
      {
         divideAndSet(operand, 'C');
         pointer += 2;
      }

      private void bdv(int operand)
      {
         divideAndSet(operand, 'B');
         pointer += 2;
      }

      // Generalized method to perform division and update the pointer
      private void divideAndSet(int operand, char register) {
         long result = (long) (registerA / Math.pow(2, getComboValue(operand)));
         switch (register) {
            case 'A':
               registerA = result;
               break;
            case 'B':
               registerB = result;
               break;
            case 'C':
               registerC = result;
               break;
            default:
               throw new IllegalArgumentException("Invalid register: " + register);
         }
      }

      private void out(int operand)
      {
         long newOutput = getComboValue(operand) % 8;
         if (outputs == null || outputs.length == 0) {
            outputs =  new long[] {newOutput};
         }
         else
         {
            outputs = LongStream.concat(Arrays.stream(outputs), LongStream.of(newOutput)).toArray();
         }
         pointer += 2;
      }

      private void bxc()
      {
         registerB = registerB ^ registerC;
         pointer += 2;
      }

      private void jnz(int operand)
      {
         pointer = registerA == 0 ? pointer + 2 : operand;
      }

      private void bst(int operand)
      {
         registerB = getComboValue(operand) % 8;
         pointer += 2;
      }

      private void bxl(int operand)
      {
         registerB = registerB ^ operand;
         pointer += 2;
      }

      public long[] getOutputs()
      {
         return outputs;
      }

      public void setRegisterA(long registerA)
      {
         this.registerA = registerA;
      }
   }


}

