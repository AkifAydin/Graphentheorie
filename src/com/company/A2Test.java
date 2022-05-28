package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;


public class A2Test {

    @Test
    public void checkRandfallCreatePerfectElimination() {
        Graph nullGraph = null;
        Assert.assertThrows(NullPointerException.class, () -> {
            A2.createPerfectElimination(nullGraph, nullGraph.getNode(0));
        });

        Graph emptyGraph = new DefaultGraph("G");
        Assert.assertThrows(IllegalArgumentException.class, () -> A2.createPerfectElimination(emptyGraph, emptyGraph.getNode(0)));
    }

    @Test
    public void checkRandfallTestElimination() {
        Graph emptyGraph = new DefaultGraph("G");
        Map<Integer, Node> sigma = new HashMap<>();
        Assert.assertTrue(A2.testElimination(emptyGraph, sigma));

        Graph g = new DefaultGraph("g");
        g.addNode("v1");
        g.addNode("v2");
        g.addEdge("v1v2", "v1", "v2");

        sigma.put(1, g.getNode(1));

        Assert.assertFalse(A2.testElimination(g, sigma));
    }

    @Test
    public void checkRandfallColorChordal() {
        Graph g = new DefaultGraph("G");
        Map<Integer, Node> emptyMap = new HashMap<>();
        Assert.assertThrows(IllegalArgumentException.class, () -> A2.colorChordaleGraph(g, emptyMap));
    }

    @Test
    public void checkRandfallChromaticNumber() {
        Graph g = new DefaultGraph("G");
        Map<Integer, Node> emptyMap = new HashMap<>();
        Assert.assertThrows(IllegalArgumentException.class, () -> A2.getChromaticNumber(g, emptyMap));

        g.addNode("v1");
        Map<Integer, Node> sigma = A2.createPerfectElimination(g, g.getNode(0));
        int cn = A2.getChromaticNumber(g, sigma);
        Assert.assertEquals(1, cn);
    }

    @Test
    public void checkRandfallGenerateChordalGraph() {
        Assert.assertEquals(0, A2.generateChordalGraph(0).getNodeCount());
        Assert.assertEquals(1, A2.generateChordalGraph(1).getNodeCount());
    }

    @Test
    public void checkRandfallGenerateCompleteGraph() {
        Assert.assertEquals(0, A2.generateCompleteGraph(0).getNodeCount());
        Assert.assertEquals(1, A2.generateCompleteGraph(1).getNodeCount());


    }


    //Richtige tests
    @Test
    public void checkChromaticNumber() {
        Random random = new Random();
        //Test complete graphs
        for (int i = 0; i < 10; i++) {
            Graph completeGraph = A2.generateCompleteGraph(random.nextInt(15) + 1);
            Node startingNode = completeGraph.getNode(0);
            Integer chromaticTreeNumber = A2.getChromaticNumber(completeGraph, A2.createPerfectElimination(completeGraph, startingNode));
            Integer expectedChromaticNumber = completeGraph.getNodeCount();
            Assert.assertEquals(expectedChromaticNumber, chromaticTreeNumber);
        }
        //Test trees
        for (int i = 0; i < 10; i++) {
            Graph tree = A2.generateTree(random.nextInt(15) + 1);
            Node startingNode = A2.getSimplizialStartingNode(tree);
            Integer chromaticTreeNumber = A2.getChromaticNumber(tree, A2.createPerfectElimination(tree, startingNode));
            Integer expectedChromaticNumber = 1;

            if (tree.getNodeCount() == 1) {
                Assert.assertEquals(expectedChromaticNumber, chromaticTreeNumber);
            } else {
                expectedChromaticNumber = 2;
                Assert.assertEquals(expectedChromaticNumber, chromaticTreeNumber);
            }
        }
    }


    @Test
    public void checkColorAndChromaticNumber() {

    }


    @Test
    public void checkGenerateChordaleGraph() {
        Random random = new Random();
        //Check node count
        for (int i = 0; i < 10; i++) {
            int randint = random.nextInt(100);
            Graph g = A2.generateCompleteGraph(randint);
            int nodes = g.getNodeCount();
            Assert.assertEquals(randint, nodes);
        }
        //Check is chordal
        for (int i = 0; i < 10; i++) {
            int randint = random.nextInt(100);
            Graph g = A2.generateChordalGraph(randint);
            Node startingNode = A2.getSimplizialStartingNode(g);

            Map<Integer, Node> elim = A2.createPerfectElimination(g, startingNode);
            Assert.assertTrue(A2.testElimination(g, elim));
        }

    }


    @Test
    public void checkGenerateCompleteGraph() {
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int randint = random.nextInt(100);
            Graph g = A2.generateCompleteGraph(randint);
            int nodes = g.getNodeCount();
            Integer expectedEdges = nodes * (nodes - 1) / 2;
            Integer actualEdges = g.getEdgeCount();

            Assert.assertEquals(randint, nodes);
            Assert.assertEquals(expectedEdges, actualEdges);
        }

    }
}