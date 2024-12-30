package aoc.year2024;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class Day24
{
   private final List<String> input;

   public Day24(List<String> input)
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
      final Map<String, Integer> values = parseValues(input);

      final List<Gate> gates = parseGates(input);
      calculateFinalValues(values, gates);
      System.out.println("Part1: "+ getNumber(values));
   }

   private void solvePartTwo()
   {
      final List<Gate> gates = parseGates(input);
      final List<Gate> faultyGates = findFaultyGates(gates);
      System.out.println("Part2: "+ getOutput(faultyGates));
   }

   private void calculateFinalValues(final Map<String, Integer> values, final List<Gate> gates) {
      final Set<String> wires = gates.stream()
            .map(Gate::getOutputWire)
            .collect(Collectors.toSet());

      while (!values.keySet().containsAll(wires)) {
         gates.forEach(gate ->
               gate.getResult(values).ifPresent(result ->
                     values.put(gate.getOutputWire(), result)));
      }
   }

   private long getNumber(final Map<String, Integer> values) {
      final String binaryRepresentation = values.keySet().stream()
            .filter(key -> key.startsWith("z"))
            .sorted(Collections.reverseOrder())
            .map(values::get)
            .map(String::valueOf)
            .collect(Collectors.joining());

      return Long.parseLong(binaryRepresentation, 2);
   }

   private List<Gate> findFaultyGates(List<Gate> gates)
   {
      return gates.stream()
            .filter(gate -> isFaulty(gate, gates))
            .toList();
   }

   private boolean isFaulty(Gate gate, List<Gate> gates) {
      String outputWire = gate.getOutputWire();
      String operator = gate.getOperator();
      String operand1 = gate.getOperand1();
      String operand2 = gate.getOperand2();

      if (outputWire.startsWith("z") && !outputWire.equals("z45")) {
         // Case 1: z-wire output should always be XOR
         return !operator.equals("XOR");
      }

      if (!outputWire.startsWith("z") && !isXYOperand(operand1) && !isXYOperand(operand2)) {
         // Case 2: Non-z-wire outputs with non-x/y inputs should be AND or OR
         return !operator.equals("AND") && !operator.equals("OR");
      }

      if (operator.equals("XOR") && isXYOperand(operand1) && isXYOperand(operand2)) {
         // Case 3: XOR with x/y inputs should output to another XOR gate
         if (!endsWith00(operand1) || !endsWith00(operand2)) {
            return !isOutputToGate(gate, gates, "XOR");
         }
      }

      if (operator.equals("AND") && isXYOperand(operand1) && isXYOperand(operand2)) {
         // Case 4: AND with x/y inputs should output to an OR gate
         if (!endsWith00(operand1) || !endsWith00(operand2)) {
            return !isOutputToGate(gate, gates, "OR");
         }
      }

      return false;
   }

   private boolean isXYOperand(String operand) {
      return operand.startsWith("x") || operand.startsWith("y");
   }

   private boolean endsWith00(String operand) {
      return operand.endsWith("00");
   }

   private boolean isOutputToGate(Gate gate, List<Gate> gates, String targetOperator) {
      String output = gate.getOutputWire();
      return gates.stream()
            .anyMatch(otherGate -> !otherGate.equals(gate)
                  && (otherGate.getOperand1().equals(output) || otherGate.getOperand2().equals(output))
                  && otherGate.getOperator().equals(targetOperator));
   }

   private String getOutput(final List<Gate> faultyGates)
   {
      return faultyGates.stream()
            .sorted()
            .map(Gate::getOutputWire)
            .collect(Collectors.joining(","));
   }

   private Map<String, Integer> parseValues(List<String> input)
   {
      return input.stream()
            .takeWhile(line -> !line.isBlank())
            .map(line -> line.split(": "))
            .collect(Collectors.toMap(
                  parts -> parts[0],
                  parts -> Integer.parseInt(parts[1]),
                  (v1, v2) -> v1, // handle duplicate keys if needed
                  TreeMap::new));
   }

   private List<Gate> parseGates(final List<String> input)
   {
      final List<Gate> gates = new ArrayList<>();
      boolean startParsing = false;
      for (final String line : input) {
         if (startParsing) {
            final String wire = line.split("-> ")[1];
            final String gate = line.split(" ->")[0];
            gates.add(new Gate(gate, wire));
         }
         if (line.isBlank()) {
            startParsing = true;
         }
      }
      return gates;
   }


   class Gate implements Comparable<Gate> {

      private String outputWire;
      private final String operand1;
      private final String operand2;
      private final String operator;

      public Gate(final String gate, final String outputWire) {
         this.outputWire = outputWire;
         operand1 = gate.split(" ")[0];
         operand2 = gate.split(" ")[2];
         operator = gate.split(" ")[1];
      }

      public Gate(final String outputWire, final String operand1, final String operand2, final String operator) {
         this.outputWire = outputWire;
         this.operand1 = operand1;
         this.operand2 = operand2;
         this.operator = operator;
      }

      public Optional<Integer> getResult(final Map<String, Integer> values) {
         if (values.containsKey(operand1) && values.containsKey(operand2)) {
            switch (operator) {
               case "AND":
                  return Optional.of(values.get(operand1) & values.get(operand2));
               case "OR":
                  return Optional.of(values.get(operand1) | values.get(operand2));
               case "XOR":
                  return Optional.of(values.get(operand1) ^ values.get(operand2));
               default:
                  throw new IllegalArgumentException("Unknown operator: " + operator);
            }
         }
         return Optional.empty();
      }

      public String getOutputWire() {
         return outputWire;
      }

      public void setOutputWire(final String wire) {
         this.outputWire = wire;
      }

      public String getOperator() {
         return operator;
      }

      public String getOperand1() {
         return operand1;
      }

      public String getOperand2() {
         return operand2;
      }

      @Override
      public String toString() {
         return operand1 + " " + operator + " " + operand2 + " -> " + outputWire;
      }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((outputWire == null) ? 0 : outputWire.hashCode());
         result = prime * result + ((operand1 == null) ? 0 : operand1.hashCode());
         result = prime * result + ((operand2 == null) ? 0 : operand2.hashCode());
         result = prime * result + ((operator == null) ? 0 : operator.hashCode());
         return result;
      }

      @Override
      public boolean equals(final Object obj) {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         final Gate other = (Gate) obj;
         if (outputWire == null) {
            if (other.outputWire != null)
               return false;
         } else if (!outputWire.equals(other.outputWire))
            return false;
         if (operand1 == null) {
            if (other.operand1 != null)
               return false;
         } else if (!operand1.equals(other.operand1))
            return false;
         if (operand2 == null) {
            if (other.operand2 != null)
               return false;
         } else if (!operand2.equals(other.operand2))
            return false;
         if (operator == null) {
            return other.operator == null;
         } else
            return operator.equals(other.operator);
      }

      @Override
      public int compareTo(final Gate o) {
         return this.getOutputWire().compareTo(o.getOutputWire());
      }
   }
}

