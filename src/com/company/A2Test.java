package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class A2Test {

    @Test
    public void checkRandfallCreatePerfectElimination(){
        Graph nullGraph = null;
        Assert.assertThrows(NullPointerException.class, () -> {
            A2.createPerfectElimination(nullGraph, nullGraph.getNode(0));
        });

        Graph emptyGraph = new DefaultGraph("G");
        Assert.assertThrows(IllegalArgumentException.class, () -> A2.createPerfectElimination(emptyGraph,emptyGraph.getNode(0)));
    }

    @Test
    public void checkRandfallTestElimination(){
        Graph emptyGraph = new DefaultGraph("G");
        Map<Integer, Node> sigma = new HashMap<>();
        Assert.assertTrue(A2.testElimination(emptyGraph,sigma));


        Graph g = new DefaultGraph("g");
        g.addNode("v1");
        g.addNode("v2");
        g.addEdge("v1v2","v1","v2");


        sigma.put(1,g.getNode(1));

        Assert.assertFalse(A2.testElimination(g,sigma));
    }

    @Test
    public void checkRandfallColorChordal(){
        Graph g = new DefaultGraph("G");
        Map<Integer, Node> emptyMap = new HashMap<>();
        Assert.assertThrows(IllegalArgumentException.class, () -> A2.colorChordaleGraph(g,emptyMap));
    }

    @Test
    public void checkRandfallChromaticNumber(){
        Graph g = new DefaultGraph("G");
        Map<Integer, Node> emptyMap = new HashMap<>();
        Assert.assertThrows(IllegalArgumentException.class, () -> A2.getChromaticNumber(g,emptyMap));

        g.addNode("v1");
        Map<Integer, Node> sigma = A2.createPerfectElimination(g, g.getNode(0));
        int cn = A2.getChromaticNumber(g,sigma);
        Assert.assertEquals(1, cn );
    }

    @Test
    public void checkRandfallGenerateChordalGraph(){
        Assert.assertEquals(0, A2.generateChordalGraph(0).getNodeCount());
        Assert.assertEquals(1, A2.generateChordalGraph(1).getNodeCount());
    }

    @Test
    public void checkRandfallGenerateCompleteGraph(){
        Assert.assertEquals(0, A2.generateCompleteGraph(0).getNodeCount());
        Assert.assertEquals(1, A2.generateCompleteGraph(1).getNodeCount());


    }


    //Richtige tests

    @Test
    public void checkGenerateCompleteGraph(){
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int randint = random.nextInt(100);
            Graph g = A2.generateCompleteGraph(randint);
            int nodes = g.getNodeCount();
            Integer expectedEdges = nodes*(nodes-1)/2;
            Integer actualEdges = g.getEdgeCount();
            //System.out.println("Expected amount of edges: " + expectedEdges + " Actual: " + actualEdges + "\n");
            Assert.assertEquals(expectedEdges, actualEdges);
        }

    }
}
