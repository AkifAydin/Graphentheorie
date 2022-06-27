package com.company;

import org.graphstream.algorithm.generator.FullGenerator;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;


public class A3 {
     static long usedTimeForGeneratingGraphFromEdge = 0;

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

        long time = System.currentTimeMillis();
        List<Node> eulerTour = generateEulerTour(minimalTree);
        usedTimeForGeneratingGraphFromEdge += System.currentTimeMillis()-time;
        removeDoubledNodes(eulerTour);


        return eulerTour;
    }


    public static List<Node> nearestInsertion(Graph g, Map<Edge, Float> weight){
        if((g.getNodeCount() * (g.getNodeCount() - 1) / 2)!=g.getEdgeCount()){
            throw new IllegalArgumentException("The given graph is not full");
        }

        List<Node> W = new LinkedList<>();
        List<Node> usedNodes = new LinkedList<>();
        Node startingNode = g.getNode(0);
        PriorityQueue<Edge> neighboringEdges = new PriorityQueue<>(Comparator.comparing(weight::get));
        neighboringEdges.addAll(startingNode.edges().toList());
        Edge closestEdge = neighboringEdges.poll();


        W.add(startingNode);
        usedNodes.add(startingNode);
        while(W.size() != g.getNodeCount()){
            Node currentNode;

            while (closestEdge != null && usedNodes.contains(closestEdge.getSourceNode()) && usedNodes.contains(closestEdge.getTargetNode())) {
                closestEdge = neighboringEdges.poll();
            }

            if(closestEdge!=null){
                if(usedNodes.contains(closestEdge.getSourceNode())){
                    currentNode = closestEdge.getTargetNode();
                }else{
                    currentNode = closestEdge.getSourceNode();
                }

                usedNodes.add(currentNode);
                neighboringEdges.addAll(currentNode.edges().toList());

                addMinimalPermutation(W, weight, currentNode);
            }
        }
        W.add(startingNode);
        return W;
    }



    public static void addMinimalPermutation(List<Node> circle, Map<Edge, Float> weight, Node nodeToBeAdded){
        float minWeightIncrease = Float.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < circle.size()-1; i++) {
            float currentWeightIncrease = weight.get(circle.get(i).getEdgeBetween(nodeToBeAdded)) + weight.get(circle.get(i+1).getEdgeBetween(nodeToBeAdded))
                    - weight.get(circle.get(i).getEdgeBetween(circle.get(i+1)));
            if(currentWeightIncrease<minWeightIncrease){
                minWeightIncrease = currentWeightIncrease;
                minIndex=i;
            }
        }
        if(circle.size()>1){
            float weightIncreaseAtEnd = weight.get(circle.get(circle.size()-1).getEdgeBetween(nodeToBeAdded)) + weight.get(circle.get(0).getEdgeBetween(nodeToBeAdded))
                    - weight.get(circle.get(circle.size()-1).getEdgeBetween(circle.get(0)));
            if(minWeightIncrease<weightIncreaseAtEnd){
                circle.add(minIndex+1, nodeToBeAdded);
            }else{
                circle.add(nodeToBeAdded);
            }
        }else {
            circle.add(minIndex + 1, nodeToBeAdded);
        }
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
     * Overall method for generating an EulerTour by calling overloaded method with same name
     * @param minimalTree
     * @return
     */
    public static List<Node> generateEulerTour(List<Edge> minimalTree) {
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
        for (int i = 0; i < circle.size()-1; i++) {
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
