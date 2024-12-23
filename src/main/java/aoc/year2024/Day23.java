package aoc.year2024;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;


public class Day23
{
   private final List<String> input;

   public Day23(List<String> input)
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
      List<Node> computers = parseInput(input);
      int nrOfTriosWithLetterT = getThreeConnectedWithLetterT(computers);
      System.out.println("Part 1: " + nrOfTriosWithLetterT);
   }

   private void solvePartTwo()
   {
      List<Node> computers = parseInput(input);
      String password = findLANPassword(computers);
      System.out.println("Part 2: " + password);
   }

   private int getThreeConnectedWithLetterT(List<Node> computers)
   {
      final Set<List<Node>> computerInThree = getAllTrios(computers);

      // Count trios with at least one node name starting with "t"
      return (int) computerInThree.stream()
            .filter(trio -> trio.stream().anyMatch(node -> node.getName().startsWith("t")))
            .count();
   }

   private Set<List<Node>> getAllTrios(List<Node> nodes) {
      final Set<List<Node>> trios = new HashSet<>();

      // Map for quick lookup
      final Map<Node, Node> nodeLookup = nodes.stream()
            .collect(Collectors.toMap(node -> node, node -> node));

      // Loop through each node
      for (final Node current : nodes) {
         for (final Node neighbor : current.getAdjacentNodes()) {
            final Node adjNode = nodeLookup.get(neighbor); // Fetch from map
            if (adjNode == null) continue; // Skip if not found

            for (final Node secondNeighbor : adjNode.getAdjacentNodes()) {
               final Node adjAdjNode = nodeLookup.get(secondNeighbor); // Fetch again
               if (adjAdjNode == null || !current.getAdjacentNodes().contains(adjAdjNode)) {
                  continue; // Skip if not part of a triangle
               }

               // Create a sorted triangle to ensure uniqueness
               final List<Node> triangle = Arrays.asList(current, adjNode, adjAdjNode);
               triangle.sort(Comparator.naturalOrder());

               // Add the sorted triangle to the set
               trios.add(triangle);
            }
         }
      }

      return trios;
   }

   private String findLANPassword(List<Node> computers)
   {
      List<Node> computersWithMaxConnection = findMaximumConnection(computers);
      // Sort the nodes for consistent ordering
      Collections.sort(computersWithMaxConnection);

      // Join the node names with commas
      return computersWithMaxConnection.stream()
            .map(Node::getName)
            .collect(Collectors.joining(","));
   }

   private List<Node> findMaximumConnection(List<Node> nodes)
   {
      int maximum = 0;
      List<Node> maxConnected = new ArrayList<>();

      // Iterate over all nodes to start a clique
      for (final Node node : nodes) {
         final List<Node> clique = new ArrayList<>();
         clique.add(node);

         // Check other nodes for potential inclusion in the clique
         for (final Node candidate : nodes) {
            if (!candidate.equals(node) &&
                  clique.stream().allMatch(cliqueNode -> cliqueNode.getAdjacentNodes().contains(candidate)))
            {
               clique.add(candidate);
            }
         }

         // Update maxConnected if the current clique is larger
         if (clique.size() > maximum) {
            maximum = clique.size();
            maxConnected = new ArrayList<>(clique);
         }
      }

      return maxConnected;
   }

   private List<Node> parseInput(final List<String> input) {
      final Map<String, Node> nodeMap = new HashMap<>();

      for (final String line : input) {
         final String[] parts = line.split("-");
         final String node1Name = parts[0];
         final String node2Name = parts[1];

         nodeMap.putIfAbsent(node1Name, new Node(node1Name));
         nodeMap.putIfAbsent(node2Name, new Node(node2Name));

         final Node node1 = nodeMap.get(node1Name);
         final Node node2 = nodeMap.get(node2Name);

         node1.addAdjacentNode(node2);
         node2.addAdjacentNode(node1);
      }

      return new ArrayList<>(nodeMap.values());
   }

   static class Node implements Comparable<Node> {
      String name;
      private Set<Node> adjacentNodes = new HashSet<>();

      public Node(String name) {
         this.name = name;
      }

      public String getName() {
         return name;
      }

      public void addAdjacentNode(Node node) {
         this.adjacentNodes.add(node);
      }

      public Set<Node> getAdjacentNodes() {
         return adjacentNodes;
      }

      @Override
      public int compareTo(Node other) {
         return name.compareTo(other.getName());
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         Node other = (Node) obj;
         if (name == null) {
            return other.name == null;
         } else
            return name.equals(other.name);
      }

      @Override
      public String toString() {
         StringBuilder adjacent = new StringBuilder();
         for (Node node : adjacentNodes) {
            adjacent.append(node.getName()).append(" ");
         }
         return "Node [name=" + name + ", adjacentNodes=" + adjacent + "]";
      }

      @Override
      public int hashCode() {
         return Objects.hash(name);
      }

   }

}

