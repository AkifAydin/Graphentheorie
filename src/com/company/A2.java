package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;


import java.awt.*;
import java.lang.invoke.MutableCallSite;
import java.util.ArrayList;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class A2 {

    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        Graph g = new MultiGraph("g");
        g.setAttribute("ui.stylesheet", "node { fill-mode: dyn-plain; }");

        g.addNode("v1").setAttribute("ui.color", Color.BLUE);
        g.addNode("v2").setAttribute("ui.color", Color.RED);
        g.addEdge("v1v2", "v1", "v2");


        g.display();

    }

    public static void lexbfs(Graph g, Node v){
        List<String> result = new ArrayList<>();
        int nodesCount = g.getNodeCount();
        g.nodes().forEach(n -> {
            n.setAttribute("label","");
        });

        for (int i = nodesCount; i >= 1; i--) {
            List<Node> unnummerierte = new ArrayList<>();
            g.nodes().forEach(n -> {
                if (n.getAttribute("label")==""){
                    unnummerierte.add(n);
                }
            });
            Optional<Node> optionalU = unnummerierte.stream().max(Comparator.comparing(n -> n.getId()));
            if (optionalU.isPresent()){
                Node u = optionalU.get();
                int index = Integer.parseInt(u.getId().substring(1));


            }else{
                System.out.println("Fehler idk");
            }


        }
    }



}
