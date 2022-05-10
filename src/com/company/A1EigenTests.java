package com.company;

import org.graphstream.graph.Graph;

import java.io.IOException;
import java.util.*;

import org.graphstream.graph.implementations.DefaultGraph;
import org.junit.Test;
import org.junit.*;

import static com.company.GraphMethods.*;
import static com.company.A1TreeTest.*;

public class A1EigenTests {

    @Test
    public void checkEinlesenSchreiben() {
        List<String> files = new ArrayList<>();
        files.add("./data/BSP1.dot");
        files.add("./data/BSP2.dot");
        files.add("./data/BSP3.dot");
        files.add("./data/BSP4.dot");
        files.add("./data/BT1.dot");
        files.add("./data/BT2.dot");
        files.add("./data/BT2a.dot");
        files.add("./data/BT3.dot");
        files.add("./data/BT4.dot");
        files.add("./data/BT5.dot");
        files.add("./data/BT6.dot");
        files.add("./data/BT7.dot");
        files.add("./data/BT8.dot");
        String outputPath = "./data/outputTest.dot";

        for (String f: files) {
            Graph graph = readGraph(f);
            writeGraph(graph,outputPath);
            Graph graphGenerated = readGraph(outputPath);
            treeEquals(graph,graphGenerated);
        }

    }

    @Test
    public void checkRandfallRead(){
        Assert.assertThrows(NullPointerException.class, () -> {
            readGraph("wrongfilepath");
        });
    }

    @Test
    public void checkRandfallWrite(){
        Assert.assertThrows(NullPointerException.class, () -> {
            writeGraph(null, "temp");
        });
    }

    @Test
    public void checkRandfallTreePrueferCode(){
        Graph g = new DefaultGraph("g");

        Assert.assertThrows(NullPointerException.class, () -> {
            fromTreeToPrueferCode(null);
        });
        Assert.assertNull(fromTreeToPrueferCode(g));

        g.addNode("v1");
        g.addNode("v2");
        g.addEdge("v1v2","v1","v2");
        long[] emptyAry = {};
        long[] generatedAry = fromTreeToPrueferCode(g);
        Assert.assertArrayEquals(generatedAry,emptyAry);
    }

    @Test
    public void checkRandfallPrueferCodeTree(){
        //Code element > code länge
        long[] ary = {15,3};
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            fromPrueferCodeToTree(ary);
        });
        //Empty array
        long[] aryTwo = {};
        Assert.assertEquals(2,fromPrueferCodeToTree(aryTwo).getNodeCount());
    }

    @Test
    public void checkRandfallGenerateTree(){
        Graph emptyGraph = new DefaultGraph("emptyGraph");
        treeEquals(emptyGraph,generateTree(0));
    }

    @Test
    public void checkEverything(){
        List<String> files = new ArrayList<>();
        files.add("./data/BSP1.dot");
        files.add("./data/BSP2.dot");
        files.add("./data/BSP3.dot");
        files.add("./data/BSP4.dot");
        files.add("./data/BT1.dot");
        files.add("./data/BT2.dot");
        files.add("./data/BT2a.dot");
        files.add("./data/BT3.dot");
        files.add("./data/BT4.dot");
        files.add("./data/BT5.dot");
        files.add("./data/BT6.dot");
        files.add("./data/BT7.dot");
        files.add("./data/BT8.dot");
        String outputPath = "./data/outputTest.dot";

        for (String f: files) {
            Graph graph = readGraph(f);
            long[] code = fromTreeToPrueferCode(graph);
            Graph genGraph = fromPrueferCodeToTree(code);
            writeGraph(genGraph,outputPath);
            readGraph(outputPath);
            treeEquals(readGraph(outputPath),graph);
        }
    }
}
