package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;


import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class A2 {

    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        Graph g = new MultiGraph("g");
        g.setAttribute("ui.stylesheet", "node { fill-mode: dyn-plain; }");

        g.addNode("a");
        g.addNode("b");
        g.addNode("c");
        g.addNode("d");
        g.addNode("e");


        g.addEdge("ac", "a", "c");
        g.addEdge("cd", "c", "d");
        g.addEdge("ad", "a", "d");
        g.addEdge("ae", "a", "e");
        g.addEdge("ab", "a", "b");
        g.addEdge("de", "d", "e");
        //bedac

        System.out.println(createPerfectElimination(g, g.getNode("b")));
        //{1=e, 2=d, 3=c, 4=a, 5=b}

        System.out.println(testElimination(g, createPerfectElimination(g, g.getNode("b"))));

        List<Node> nodes = colorChordaleGraph(g, createPerfectElimination(g, g.getNode("b"))).nodes().toList();
        nodes.forEach(n -> {
            System.out.println("Node: "+ n.getId() + "   Color: " + n.getAttribute("color"));
        });
        System.out.println("Anzahl Farben: " + getChromaticNumber(g,createPerfectElimination(g, g.getNode("b"))));


        //Graph graph = generateCompleteGraph(50);
        //graph.display();
    }


    /**
     * Berechnet ein perfektes Eliminationsschema mithilfe des lex-bfs
     *
     * @param graph        gegebener Graph
     * @param startingNode node welche Anfagns entfernt werden soll
     * @return Liste mit Nodes
     */
    public static Map<Integer, Node> createPerfectElimination(Graph graph, Node startingNode) {

        if(startingNode==null){
            throw new IllegalArgumentException("StartingNode existiert nicht");
        }
        if(graph.getNode(startingNode.getId())==null){
            throw new IllegalArgumentException("StartingNode wurde nicht gefunden");
        }


        // Initialize
        Graph g = Graphs.clone(graph);
        Map<Integer, Node> sigma = new HashMap<>();
        List<Node> unnumeriert = new ArrayList<>();
        g.nodes().forEach(unnumeriert::add);
        // Prepare nodes
        g.nodes().forEach(n -> n.setAttribute("label", "0"));
        g.nodes().forEach(n -> n.setAttribute("marke", ""));

        Node u = g.getNode(startingNode.getId());

        for (int i = g.getNodeCount(); i > 0; i--) {
            sigma.put(i, graph.getNode(u.getId()));
            unnumeriert.remove(u);
            u.setAttribute("marke", "" + i);

            int finalI = i;
            u.neighborNodes().forEach((Node n) -> {
                if (n.getAttribute("marke") != "") {
                    return;
                }
                if (n.getAttribute("label") == "0"){
                    n.setAttribute("label", String.valueOf(finalI));
                    return;
                }
                n.setAttribute("label", n.getAttribute("label") + String.valueOf(finalI));
            });
            //Long da bei vollständigen Graphen sehr viele Kanten entstehen
            unnumeriert = unnumeriert.stream().sorted(Comparator.comparing(n -> Long.parseLong((String) n.getAttribute("label")))).collect(Collectors.toList());
            Collections.reverse(unnumeriert);
            if (!unnumeriert.isEmpty()) {
                u = unnumeriert.get(0);
            }
        }
        return sigma;
    }

    /**
     * Tests if a given elimination scheme is correct for given graph
     * @param g given graph
     * @param sigma given elimination scheme
     * @return true or false
     */
    public static boolean testElimination(Graph g, Map<Integer, Node> sigma) {
        if(sigma.size()!=g.getNodeCount()){
            return false;
        }


        //Graph ga = Graphs.clone(graph);
        Map<Node, List<Node>> A = new HashMap<>();

        g.nodes().forEach(n -> {
            A.put(n, new ArrayList<>());
        });

        for (int i = 1; i < g.getNodeCount(); i++) {
            Node u = sigma.get(i);
            List<Node> X = u.neighborNodes().filter(v -> getKeyByValue(sigma, u) < getKeyByValue(sigma, v)).collect(Collectors.toList());
            if (!X.isEmpty()) {
                Node w = X.stream().min(Comparator.comparingInt((Node n) -> getKeyByValue(sigma, n))).get();
                X.remove(w);
                List<Node> newList = new ArrayList<>();
                newList.addAll(X);
                A.put(w, newList);
            }

            u.neighborNodes().forEach(n -> {
                if (A.containsKey(u)) {
                    A.get(u).remove(n);
                }
            });
            if (A.containsKey(u)) {
                if (!A.get(u).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Returns the graph with added color attributes for minimal colors.
     * @param graph input graph
     * @return Graph colored minimally
     */
    public static Graph colorChordaleGraph(Graph graph, Map<Integer, Node> sigma){
        if(graph.getNodeCount()==0){
            throw new IllegalArgumentException("Graph ist leer");
        }
        if(sigma.isEmpty()){
            throw new IllegalArgumentException("Eliminations schema ist leer");
        }

        if(!testElimination(graph, sigma)){
            throw new IllegalArgumentException("Graph ist nicht chordal");
        }
        //sigma.get(sigma.size()).setAttribute("color","1");
        graph.getNode(sigma.get(sigma.size()).getId()).setAttribute("color","1");

        for (int i = sigma.size()-1; i > 0; i--) {
            Node currentNode = sigma.get(i);
            List<String> usedColors = new ArrayList<>();

            currentNode.neighborNodes().forEach(n -> {
                if (graph.getNode(n.getId()).getAttribute("color") != null){
                    usedColors.add(String.valueOf(graph.getNode(n.getId()).getAttribute("color")));
                }
            });
            //Color first not used color
            int counter = 1;
            while (true){
                if (!(usedColors.contains(""+counter))){
                    //System.out.println("Counter: " + counter + " usedColors: " + usedColors);
                    graph.getNode(currentNode.getId()).setAttribute("color",""+counter);
                    break;
                }
                counter++;
            }
        }
        return graph;
    }

    /**
     * Returns chromatic number for given graph and given perfect elimination scheme
     * @param graph,sigma given Graph, given elimination scheme
     * @return chromatic number
     */
    public static Integer getChromaticNumber(Graph graph, Map<Integer, Node> sigma){
        if(graph.getNodeCount()==0){
            throw new IllegalArgumentException("Graph ist leer");
        }
        if (sigma.isEmpty()){
            throw new IllegalArgumentException("Eliminations schema ist leer");
        }

        Graph g = Graphs.clone(graph);

        if (testElimination(g,sigma)){
            Graph gColored = colorChordaleGraph(g,sigma);
            //Farben zählen
            Optional<Node> maxNodeOpt = gColored.nodes().max(Comparator.comparingInt(
                    (Node n) -> Integer.parseInt(String.valueOf(n.getAttribute("color")))));
            if(maxNodeOpt.isPresent()){
                Node maxNode = maxNodeOpt.get();
                return Integer.parseInt(String.valueOf(maxNode.getAttribute("color")));
            }else{
                return -1;
            }
        }else{
            throw new IllegalArgumentException("Eliminationsschema nicht korrekt");
        }
    }

    /**
     * Generates simple chordal graph with nodeAmount nodes
     * @param nodeAmount amount of nodes in generated Graph
     * @return generated graph
     */
    public static Graph generateChordalGraph(Integer nodeAmount){
        Graph g = new DefaultGraph("G");
        if(nodeAmount==0){
            return g;
        }

        Random random = new Random();
        //Add first node
        g.addNode("v1");

        if(nodeAmount>1){
            g.addNode("v2");
            g.addEdge("v1v2","v1","v2");

            for (int i = 2; i < nodeAmount; i++) {
                int randInt = random.nextInt(2);
                if(randInt==1){
                    int nodeCount = g.getNodeCount();
                    int newNodeCount = nodeCount+1;
                    int randNodeInt = random.nextInt(nodeCount);
                    Node v = g.getNode(randNodeInt); // Indizes bei 0?
                    g.addNode("v"+newNodeCount);
                    g.addEdge(v.getId()+"v"+newNodeCount,v.getId(),"v"+newNodeCount);
                }else{
                    int edgeCount = g.getEdgeCount();
                    int newEdgeCount = edgeCount+1;
                    int randEdgeInt = random.nextInt(edgeCount);
                    Edge vw = g.getEdge(randEdgeInt);
                    Node v = vw.getSourceNode();
                    Node w = vw.getTargetNode();

                    Node u = g.addNode("v"+(g.getNodeCount()+1));
                    g.addEdge(v.getId()+u.getId(),v,u);
                    g.addEdge(w.getId()+u.getId(),w,u);
                }
            }
        }
        return g;
    }

    public static Graph generateCompleteGraph(int nodeAmount){
        Graph g = new DefaultGraph("G");

        for(int i = 1; i <= nodeAmount; i++) {
            g.addNode("v"+i);
        }

        g.nodes().forEach(n -> {
            for (int i = 1; i <= nodeAmount; i++) {
                if (!(("v" + i).equals(n.getId()))){
                    if (g.getEdge(n.getId()+"v"+i)==null && g.getEdge("v"+i+n.getId())==null){
                        g.addEdge(n.getId()+"v"+i,n.getId(),"v"+i);
                    }
                }
            }
        });
        return g;
    }
    /**
     * Hilfsmethode!
     * Returns first matched key from input value
     *
     * @param map   map to search
     * @param value value to search matched key
     * @return first matched key
     */
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        throw new NullPointerException();
    }

    /**
     * Hilfsmethode!
     * Looks for a simplicial node to start the perfect elimination with.
     * @param g chordal graph
     * @return first simplicial node found
     */
    public static Node getSimplizialStartingNode(Graph g){
        List<Node> startingNodeList = new ArrayList<>();
        AtomicBoolean foundNotConnectedNode = new AtomicBoolean(false);
        for (int i = 0; i < g.getNodeCount(); i++) {
            //get random node
            Node currentNode = g.getNode(i);
            currentNode.neighborNodes().forEach(v -> {
                currentNode.neighborNodes().forEach(u -> {
                    if (v != u) {
                        if (!v.neighborNodes().toList().contains(u)) {
                            foundNotConnectedNode.set(true);
                        }
                    }
                });
            });
            if (!foundNotConnectedNode.get()) {
                startingNodeList.add(currentNode);
                break;
            }
            foundNotConnectedNode.set(false);
        }
        if(startingNodeList.isEmpty()){
            throw new IllegalArgumentException("Eingegebener Graph nicht chordal");
        }
        return startingNodeList.get(0);
    }


    /**
     * Hilfsmethode!
     * Generiert einen Baum mit n Nodes und zufälliger Struktur
     * @param n
     * @return
     */
    public static Graph generateTree(int n){
        Graph result = new DefaultGraph("G");
        Random rand = new Random();
        for (int i = 1; i <= n; i++) {
            result.addNode("v"+i).setAttribute("label",""+i);
            Node newNode = result.getNode("v"+i);
            if(i>1){
                //Es darf nicht die neuste Node ausgewählt werden also i-1,
                // dann wird noch 1 addiert, da die nextInt Methode bei 0 anfängt
                int randInt = rand.nextInt(i-1)+1;
                //System.out.println(randInt);
                Node randomNode = result.getNode("v"+randInt);
                if (result.getEdge(""+i+(randInt))==null){
                    result.addEdge("v"+i+"v"+(randInt),newNode,randomNode);
                }
            }
        }
        return result;
    }
}
