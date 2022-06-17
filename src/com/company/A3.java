package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkSVG;

import java.io.IOException;
import java.util.*;


public class A3 {

    /**
     * Returns minimal tree
     * @param g graph
     * @param c map Edge -> weight value
     * @return List of edges that represent the tree
     */
    public static List<Edge> minimalerSpannbaum(Graph g, HashMap<Edge, Float> c){
        int randInt = new Random().nextInt(g.getNodeCount());
        Node randomStartingNode = g.getNode(randInt);


        List<Edge> ET = new ArrayList<>();
        List<Node> S = new ArrayList<>();
        S.add(randomStartingNode);


        while(S != g.nodes().toList()){
            List<Edge> allConnectedEdges = new ArrayList<>();

            S.forEach((Node n) -> allConnectedEdges.addAll(n.edges().toList()));
            List<Edge> allEdgesWithOneNodeInV = allConnectedEdges.stream().filter((Edge e) -> {
                Node source = e.getSourceNode();
                Node destination = e.getTargetNode();
                //return true if either is not included in S
                return !S.contains(source) || !S.contains(destination);
            }).toList();
            System.out.println();
            System.out.println("S: " + S);

            System.out.println("all edges: " + allEdgesWithOneNodeInV);
            Optional<Edge> minimalEdgeOpt = allEdgesWithOneNodeInV.stream().min(Comparator.comparing(c::get));

            if(minimalEdgeOpt.isPresent()){
                Edge minimalEdge = minimalEdgeOpt.get();
                ET.add(minimalEdge);
                //add node that was not in S already
                if(S.contains(minimalEdge.getSourceNode())){
                    S.add(minimalEdge.getTargetNode());
                }else{
                    S.add(minimalEdge.getSourceNode());
                }

            }else{
                return ET;
               // throw new IllegalArgumentException("Minimale Kante konnte nicht gefunden werden");
            }
        }
        return ET;
    }


    public static void main(String[] args) throws IOException {
        System.setProperty("org.graphstream.ui", "swing");
        Graph g = new DefaultGraph("g");

        g.addNode("A").setAttribute("x","A");
        g.addNode("B");
        g.addNode("C");
        g.addNode("D");
        g.addNode("E");
        g.addNode("F");

        g.addEdge("AB","A","B");
        g.addEdge("AC","A","C");
        g.addEdge("AD","A","D");
        g.addEdge("BE","B","E");
        g.addEdge("BD","B","D");
        g.addEdge("DE","D","E");
        g.addEdge("DC","D","C");
        g.addEdge("CF","C","F");
        g.addEdge("EF","E","F");


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



    }
}
