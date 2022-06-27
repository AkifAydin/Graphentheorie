package com.company;

import org.graphstream.graph.*;

import java.util.*;

public class Grid {

    private List<List<Node>> gitterNetz;
    private Graph graph;
    private Integer gittersize;
    private Map<Node, int[]> indizes = new HashMap<>();



    public Grid(Graph g){
        this.graph = g;
        this.gittersize = (int)Math.ceil(Math.sqrt(g.getNodeCount()));
        this.gitterNetz = new ArrayList<>(new ArrayList<>());

        for (int i = 0; i < gittersize; i++) {
            gitterNetz.add(new ArrayList<>());
            for (int j = 0; j < gittersize; j++) {
                gitterNetz.get(i).add(null);
            }
        }
    }

    public void randomFill(){
        graph.nodes().forEach(n -> {
            Random random = new Random();
            boolean check = false;


            while(!check){
                int randX = random.nextInt(gittersize);
                int randY = random.nextInt(gittersize);
                if(gitterNetz.get(randX).get(randY)==null){
                    gitterNetz.get(randX).set(randY, n);

                    indizes.put(n, new int[]{randX, randY});
                    check = true;
                }
            }
        });
    }

    public Map<Edge, Float> calculateManhattan(){
        Map<Edge, Float> c = new HashMap<>();

        graph.forEach(u -> {
            u.neighborNodes().forEach(v -> {
                int[] posU = indizes.get(u);
                int[] posY = indizes.get(v);

                Edge edgeUV = u.getEdgeBetween(v);

                int distanceX = Math.abs(posU[0] - posY[0]);
                int distanceY = Math.abs(posU[1] - posY[1]);

                c.put(edgeUV, (float)distanceX+distanceY);
            });
        });
        return c;
    }


    public void printNetz(){
        gitterNetz.forEach(list -> {
            StringBuilder sb = new StringBuilder();
            list.forEach(n -> {
                if(n!=null){
                    sb.append(n.getId());
                    sb.append("\t");
                }else{
                    sb.append("NO");
                    sb.append("\t");
                }
            });
            System.out.println(sb);
        });
    }

}
