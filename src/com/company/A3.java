package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;


public class A3 {

    /**
     * Returns minimal tree
     *
     * @param g graph
     * @param c map Edge -> weight value
     * @return List of edges that represent the tree
     */
    public static List<Edge> minimalerSpannbaum(Graph g, HashMap<Edge, Float> c) {
        int randInt = new Random().nextInt(g.getNodeCount());
        Node randomStartingNode = g.getNode(randInt);


        List<Edge> ET = new ArrayList<>();
        List<Node> S = new ArrayList<>();
        S.add(randomStartingNode);


        while (S != g.nodes().toList()) {
            List<Edge> allConnectedEdges = new ArrayList<>();

            S.forEach((Node n) -> allConnectedEdges.addAll(n.edges().toList()));
            List<Edge> allEdgesWithOneNodeInV = allConnectedEdges.stream().filter((Edge e) -> {
                Node source = e.getSourceNode();
                Node destination = e.getTargetNode();
                //return true if either is not included in S
                return !S.contains(source) || !S.contains(destination);
            }).toList();
            Optional<Edge> minimalEdgeOpt = allEdgesWithOneNodeInV.stream().min(Comparator.comparing(c::get));

            if (minimalEdgeOpt.isPresent()) {
                Edge minimalEdge = minimalEdgeOpt.get();
                ET.add(minimalEdge);
                //add node that was not in S already
                if (S.contains(minimalEdge.getSourceNode())) {
                    S.add(minimalEdge.getTargetNode());
                } else {
                    S.add(minimalEdge.getSourceNode());
                }

            } else {
                return ET;
                // throw new IllegalArgumentException("Minimale Kante konnte nicht gefunden werden");
            }
        }
        return ET;
    }


    /**
     * returns the shortest round trip W
     *
     * @param g graph
     * @param c map Edge -> weight value
     * @return the shortest round trip W
     */
    public static List<Node> nearestInsertion(Graph g, Map<Edge, Float> c) {
        List<Node> W = new ArrayList<>();

        //Random Startingpoint
        int randNodeIndex = new Random().nextInt(g.getNodeCount() + 1);
        Node startingNode = g.getNode(randNodeIndex);

        List<Node> usedNodes = new ArrayList<>();

        // [startingNode, v1, v2, v3...]   -> später startingNode ans Ende  [startingNode, ... , startingNode]
        W.add(startingNode);


        while (W.size() != g.getNodeCount()) {

            AtomicReference<Float> minD = new AtomicReference<>(Float.MAX_VALUE);
            final Node[] minNode = new Node[1];
            List<Node> finalW = W;
            g.nodes().forEach((Node v) -> {
                finalW.forEach((Node u) -> {
                    if (u != v && v!=startingNode && !finalW.contains(v)) {
                        // node u from circle W,  node v potential nearest node from graph
                        Edge edgeUV = u.getEdgeBetween(v);
                        Float edgeWeight = c.get(edgeUV);

                        System.out.println("Nodes -> u: " + u + " v: " + v + "   Edge:" + edgeUV + " with weight: " + edgeWeight);
                        System.out.println();
                        if (edgeWeight < minD.get()) {
                            minD.set(edgeWeight);
                            minNode[0] = v;
                        }
                    }
                });
            });
            W.add(minNode[0]);
            System.out.println("W: " + W + " minNode: " + minNode[0]);

            //min permutation
            List<Node> circleWithoutStartingNode = W.subList(1, W.size());
            Stream<List<Node>> permutations = permutations(circleWithoutStartingNode);
            AtomicReference<List<Node>> minPermutation = new AtomicReference<>();
            AtomicReference<Float> minWeightCost = new AtomicReference<>(Float.MAX_VALUE);
            AtomicReference<Float> weightCost = new AtomicReference<>(0f);
            permutations.forEach((List<Node> permutation) -> {
                weightCost.set(0f);
                weightCost.updateAndGet(v -> v + c.get(startingNode.getEdgeBetween(permutation.get(0))));
                for (int i = 0; i < permutation.size() - 2; i++) {
                    int finalI = i;

                    weightCost.updateAndGet(v -> v + c.get(permutation.get(finalI).getEdgeBetween(permutation.get(finalI + 1))));
                }

                weightCost.updateAndGet(v -> v + c.get(permutation.get(permutation.size() - 1).getEdgeBetween(startingNode)));
                minPermutation.set(permutation);
                System.out.println("Starting Node: " + startingNode+ "  Permutation: " + permutation+ " with weight " + weightCost.get());
            });
            if (weightCost.get() < minWeightCost.get()) {
                W = new ArrayList<>();
                minWeightCost.set(weightCost.get());
                W.add(startingNode);
                W.addAll(minPermutation.get());
                System.out.println("Weight: " + weightCost.get());
            }
            System.out.println("W: " + W + " with weight " + minWeightCost.get());
            System.out.println();
        }

        W.add(startingNode);
        return W;
    }


    /**
     * https://stackoverflow.com/questions/14132877/order-array-in-every-possible-sequence
     *
     * @param input
     * @return
     */
    public static Stream<List<Node>> permutations(List<Node> input) {
        if (input.size() == 1) {
            return Stream.of(new LinkedList<>(input));
        }
        return input.stream()
                .flatMap(first -> permutations(input.stream()
                        .filter(a -> !a.equals(first))
                        .toList())
                        .map(LinkedList::new)
                        .peek(l -> l.addFirst(first)));
    }


    public static void main(String[] args) throws IOException {
        System.setProperty("org.graphstream.ui", "swing");
        Graph g = new DefaultGraph("g");

        g.addNode("A");
        g.addNode("B");
        g.addNode("C");
        g.addNode("D");
        g.addNode("E");
        //g.addNode("F");

        g.addEdge("AB", "A", "B");
        g.addEdge("AC", "A", "C");
        g.addEdge("AD", "A", "D");
        g.addEdge("AE", "A", "E");
        g.addEdge("BE", "B", "E");
        g.addEdge("BC", "B", "C");
        g.addEdge("BD", "B", "D");
        g.addEdge("CD", "C", "D");
        g.addEdge("CE", "C", "E");
        g.addEdge("DE", "D", "E");


        Map<Edge, Float> c = new HashMap<>();

        c.put(g.getEdge("AB"), 5f);
        c.put(g.getEdge("AE"), 20f);
        c.put(g.getEdge("AD"), 15f);
        c.put(g.getEdge("AC"), 10f);
        c.put(g.getEdge("BC"), 35f);
        c.put(g.getEdge("BD"), 40f);
        c.put(g.getEdge("BE"), 45f);
        c.put(g.getEdge("CD"), 25f);
        c.put(g.getEdge("CE"), 30f);
        c.put(g.getEdge("DE"), 50f);

        System.out.println(nearestInsertion(g, c));


/*

        //FileSinkSVG fileSinkSVG = new FileSinkSVG();

        //fileSinkSVG.writeAll(g, "test.svg");

        HashMap<Edge, Float> c = new HashMap<>();

        c.put(g.getEdge("AB"),1f);
        c.put(g.getEdge("AC"),2f);
        c.put(g.getEdge("AD"),7f);
        c.put(g.getEdge("BE"),4f);
        c.put(g.getEdge("BD"),6f);
        c.put(g.getEdge("DE"),4f);
        c.put(g.getEdge("DC"),3f);
        c.put(g.getEdge("CF"),5f);
        c.put(g.getEdge("EF"),2f);

        List<Edge> spannbaum = minimalerSpannbaum(g,c);
        System.out.println(spannbaum);
        //g.display();
*/


    }
}
