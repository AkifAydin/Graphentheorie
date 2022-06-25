package com.company;

import org.graphstream.algorithm.generator.FullGenerator;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class A3 {

    /**
     * Returns minimal tree with the use of Prim algorithm
     *
     * @param g graph
     * @param c map Edge -> weight value
     * @return List of edges that represent the tree
     */
    public static List<Edge> minimalSpanningTree(Graph g, Map<Edge, Float> c) {
        Node startingNode = g.getNode(0);


        List<Edge> ET = new ArrayList<>();
        List<Node> S = new ArrayList<>();
        S.add(startingNode);


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
     * Returns the minmal tour of the graph via a List of nodes
     * @param graph TSP graph with weights as map c
     * @param c weight map
     * @return List<Node>
     */
    public static List<Node> minimumSpanningTreeHeuristic(Graph graph, Map<Edge, Float> c){
        List<Edge> minimalTree = minimalSpanningTree(graph, c);
        List<Node> eulerTour = generateEulerTour(minimalTree);

        removeDoubledNodes(eulerTour);


        return eulerTour;
    }


    /**
     * returns the shortest round trip W
     *
     * @param g graph
     * @param c map Edge -> weight value
     * @return the shortest round trip W
     */
    public static List<Node> nearestInsertion(Graph g, Map<Edge, Float> c) {
        if((g.getNodeCount() * (g.getNodeCount() - 1) / 2)!=g.getEdgeCount()){
            throw new IllegalArgumentException("The given graph is not full");
        }


        List<Node> W = new ArrayList<>();

        Node startingNode = g.getNode(0);

        List<Node> usedNodes = new ArrayList<>();

        // [startingNode, v1, v2, v3...]   -> später startingNode ans Ende  [startingNode, ... , startingNode]
        W.add(startingNode);


        while (W.size() != g.getNodeCount()) {

            AtomicReference<Float> minD = new AtomicReference<>(Float.MAX_VALUE);
            final Node[] minNode = new Node[1];
            List<Node> finalW = W;
            g.nodes().forEach((Node v) -> {
                finalW.forEach((Node u) -> {
                    if (u != v && v != startingNode && !finalW.contains(v)) {
                        // node u from circle W,  node v potential nearest node from graph
                        Edge edgeUV = u.getEdgeBetween(v);
                        Float edgeWeight = c.get(edgeUV);

                        if (edgeWeight < minD.get()) {
                            minD.set(edgeWeight);
                            minNode[0] = v;
                        }
                    }
                });
            });
            W.add(minNode[0]);

            //min permutation
            List<Node> circleWithoutStartingNode = W.subList(1, W.size());
            Stream<List<Node>> permutations = permutations(circleWithoutStartingNode);
            AtomicReference<List<Node>> minPermutation = new AtomicReference<>();
            AtomicReference<Float> minWeightCost = new AtomicReference<>(Float.MAX_VALUE);
            AtomicReference<Float> weightCost = new AtomicReference<>(0f);
            AtomicReference<List<Node>> currentNewW = new AtomicReference<>();
            permutations.forEach((List<Node> permutation) -> {
                weightCost.set(0f);
                weightCost.updateAndGet(v -> v + c.get(startingNode.getEdgeBetween(permutation.get(0))));
                for (int i = 0; i < permutation.size() - 1; i++) {
                    int finalI = i;
                    weightCost.updateAndGet(v -> v + c.get(permutation.get(finalI).getEdgeBetween(permutation.get(finalI + 1))));
                }

                weightCost.updateAndGet(v -> v + c.get(permutation.get(permutation.size() - 1).getEdgeBetween(startingNode)));
                minPermutation.set(permutation);
                if (weightCost.get() < minWeightCost.get()) {
                    currentNewW.set(new ArrayList<>());
                    minWeightCost.set(weightCost.get());
                    currentNewW.get().add(startingNode);
                    currentNewW.get().addAll(minPermutation.get());
                }
            });
            W = currentNewW.get();
        }
        W.add(startingNode);
        return W;
    }


    /**
     * Generates a complete Graph with a weight Map   edge ->  float
     * @param n amount of nodes
     * @return map of Graph -> weightMap
     */
    public static Map<Graph, Map<Edge,Float>> generateCompleteGraphWithTSP(int n) {
        Graph g = new DefaultGraph("TSPGraph");
        Map<Graph, Map<Edge,Float>> result = new HashMap<>();

        FullGenerator graphGenerator = new FullGenerator();
        graphGenerator.addSink(g);
        graphGenerator.begin();

        for (int i = 0; i < n; i++) {
            graphGenerator.nextEvents();
        }
        graphGenerator.end();

        Grid grid = new Grid(g);
        grid.randomFill();
        Map<Edge, Float> weightMap = grid.calculateManhattan();

        g.edges().forEach((Edge e) -> {
            e.setAttribute("weight", weightMap.get(e));
        });
        result.put(g,weightMap);
        return result;
    }


    /**
     * Helper method!
     * Generates stream of Lists for every possible permutation/order of nodes in a list
     * https://stackoverflow.com/questions/14132877/order-array-in-every-possible-sequence
     *
     * @param input list
     * @return Stream of every possible list ordering
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

    /**
     * Overall method for generating an EulerTour by calling overloaded method with same name
     * @param minimalTree
     * @return
     */
    public static List<Node> generateEulerTour(List<Edge> minimalTree) {
        System.out.println(minimalTree);
        Graph tree = generateGraphFromEdgeList(minimalTree);
       // generateEulerGraphFromTree(tree);
        List<Node> eulerTour = new ArrayList<>();
        List<Node> usedNodes = new ArrayList<>();

        generateEulerTour(tree.getNode(0), eulerTour, usedNodes);


        return eulerTour;
    }

    /**
     * Overloaded method for recursively generating EulerTour
     * @param currentNode
     * @param eulerTour
     * @param usedNodes
     */
    public static void generateEulerTour(Node currentNode, List<Node> eulerTour, List<Node> usedNodes) {
        usedNodes.add(currentNode);
        eulerTour.add(currentNode);

        currentNode.neighborNodes().forEach((Node neighbor) -> {
            if (!usedNodes.contains(neighbor)) {
                generateEulerTour(neighbor, eulerTour, usedNodes);
                eulerTour.add(currentNode);
            }
        });

    }

    /**
     * Helper method!
     * Removes all nodes from list except their first appearance and excluding the last node.
     * @param eulerTour euler tour with doubled nodes
     * @return List of nodes
     */
    public static List<Node> removeDoubledNodes(List<Node> eulerTour){

        List<Node> usedNodes = new ArrayList<>();
        Node lastNode = eulerTour.get(eulerTour.size()-1);

        int counter = 0;
        while (counter < eulerTour.size()) {
            Node n = eulerTour.get(counter);
            if (!usedNodes.contains(n)) {
                usedNodes.add(n);
                counter++;
            } else {
                eulerTour.remove(counter);
            }
        }
        eulerTour.add(lastNode);
        return eulerTour;
    }

    /**
     * Helper method!
     * Destructively transforms given tree into eulerGraph by doubling all edges
     * @param tree
     */
    public static void generateEulerGraphFromTree(Graph tree) {
        tree.edges().forEach((Edge e) -> {
            Node sourceNode = e.getSourceNode();
            Node targetNode = e.getTargetNode();

            tree.addEdge(e.getId() + "2", sourceNode.getId(), targetNode.getId());
        });
    }

    /**
     * Helper method!
     * Takes a List of edges and returns a Graph with all edges and nodes of the edges added
     * @param edges
     * @return
     */
    public static Graph generateGraphFromEdgeList(List<Edge> edges) {
        Graph g = new DefaultGraph("tree");

        for (Edge e : edges) {
            Node sourceNode = e.getSourceNode();
            Node targetNode = e.getTargetNode();

            if(g.getNode(sourceNode.getId())==null) {
                g.addNode(sourceNode.getId());
            }
            if(g.getNode(targetNode.getId())==null) {
                g.addNode(targetNode.getId());
            }

            g.addEdge(sourceNode.getId() + targetNode.getId(), sourceNode.getId(), targetNode.getId());
        }
        return g;
    }

    /**
     * Hepler method!
     * Returns weight of given circle, ignores connection of last node with first node
     * @param g
     * @param circle
     * @param c
     * @return
     */
    public static Float getWeightOfCircle(Graph g, List<Node> circle, Map<Edge, Float> c){
        Float counter = 0f;
        for (int i = 0; i < circle.size()-2; i++) {
            Float currentWeight = c.get(g.getNode(circle.get(i).getId()).getEdgeBetween(g.getNode(circle.get(i+1).getId())));
            counter+=currentWeight;
        }
        return counter;
    }

    public static Float getEdgesum(List<Edge> spanningTree, Map<Edge, Float> c){
        Float counter = 0f;
        for (int i = 0; i < spanningTree.size(); i++) {
            counter+=c.get(spanningTree.get(i));
        }
        return counter;
    }

}
