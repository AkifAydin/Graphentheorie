package com.company;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GraphMethods {
    public static void main(String[] args) throws IOException {
        System.setProperty("org.graphstream.ui", "swing");

        Graph tree = generateTree(70);
        tree.display();
    }


    /**
     * Wandelt Prüfer-Tupel in einen Baum um
     * @param code
     * @return generierter Graph
     */
    public static Graph fromPrueferCodeToTree(long[] code) {
        Graph result = new DefaultGraph("g");
        //Vorbedingungen
        if (code == null){
            result.addNode("v1").setAttribute("label",""+1);
            return result;
        }
        for (long i : code) {
            if(i>code.length+2){
                throw new IllegalArgumentException("Tupel Wert " + i + " zu groß");
            }
        }
        //v und t initialisieren
        List<Long> t = Arrays.stream(code).boxed().collect(Collectors.toList()); // Tupel
        List<Long> v = new ArrayList<>(); // Knoten mit Länge von t + 2
        for (long i = 1; i < t.size() + 3; i++) {
            v.add(i);
            result.addNode("v" + i).setAttribute("label",String.valueOf(i));
        }
        //Solange bis das Tupel leer ist:
        while(!t.isEmpty()){
            for (Long i : t) {
                v.remove(i);    //Mengendifferenz V \ T bilden
            }
            Long te = t.get(0);
            Long ve = v.get(0);

            // Erste Element von V und T verbinden
            result.addEdge("v"+te+"v"+ve,"v"+te,"v"+ve);
            //Verwendete Knoten entfernen
            v.remove(ve);
            v.add(0,t.get(0)); //Element aus dem Tupel darf wieder verwendet werden
            v = v.stream().sorted().collect(Collectors.toList());
            t.remove(t.get(0));
        }
        // Letzten zwei verbinden
        result.addEdge("v"+v.get(0)+"v"+v.get(1),"v"+v.get(0),"v"+v.get(1));

        return result;
    }


    /**
     * Berechnet das zugehörige Prüfer-Tupel für einen Baum
     * @param graph
     * @return Prüfer Tupel als long[]
     */
    public static long[] fromTreeToPrueferCode(Graph graph) {
        List<Long> result = new ArrayList<>(); //Ergebnis Tupel Liste
        List<Node> leafSorted; //Liste mit Blättern sortiert

        //Vorbedingungen
        if (graph == null) {
            throw new NullPointerException("Graph ist null");
        }
        Graph g = Graphs.clone(graph);
        if (g.getNodeCount() < 2){
            return null;
        }
        if (g.getNodeCount() == 2) {
            return new long[]{};
        }

        int tupelSize = g.getNodeCount() - 2;
        while (!(result.size() == tupelSize)) {
            List<Node> leaf = new ArrayList<>();
            g.nodes().forEach(n -> {
                if (n.getDegree() == 1) {   //Knoten mit Grad=1 sammeln
                    leaf.add(n);
                }
            });
            // Sortieren, damit die niedrigste KnotenID als erstes verwendet wird
            leafSorted = leaf.stream().sorted
                    (Comparator.comparing(n -> Integer.parseInt((String)n.getAttribute("label")))).collect(Collectors.toList());
            Node n = leafSorted.get(0);
            // Knoten der mit dem Blatt verbunden ist bekommen
            Long opposite = Long.parseLong(n.getEdge(0).getOpposite(n).getAttribute("label").toString());
            result.add(opposite);
            g.removeNode(n);
        }
        // Konvertieren zu Array
        long[] lAry = new long[result.size()];
        for (long i=0; i<result.size(); i++) {
            lAry[(int) i] = result.get((int) i);
        }
        return lAry;
    }


    /**
     * Liest eine DOT Datei und gibt den zugehörigen Baum zurück
     * @param filePath
     * @return zugehöriger Graph
     */
    public static Graph readGraph(String filePath) {

        Graph graph = new DefaultGraph("g");

        try {
            FileSource fs = FileSourceFactory.sourceFor(filePath);
            fs.addSink(graph);
            fs.begin(filePath);

            //Schleife befüllt den Graphen
            while (fs.nextEvents()) {
                System.out.println("Graph Event eingefügt\n");
            }

            fs.end();
            fs.removeSink(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Falls der Graph immernoch leer ist, befindet sich vermutlich ein PrüferTupel im "code" Attribut
        if(graph.getNodeCount()==0){
            String code = (String) graph.getAttribute("code");
            String[] strAry = code.split(",");
            long[] codeAry = new long[strAry.length];
            for (int i=0; i < strAry.length; i++) {
                codeAry[i] = Long.parseLong(strAry[i]);
            }
            //Um das PrüferTupel zu verwenden rufen wir unsere Methode auf
            return fromPrueferCodeToTree(codeAry);
        }
        return graph;
    }


    /**
     * Schreibt einen Graphen in eine DOT Datei
     * @param graph
     * @param outputStr
     */
    public static void writeGraph(Graph graph, String outputStr) {
        FileSinkDOT fsDOT = new FileSinkDOT();
        try {
            fsDOT.writeAll(graph, outputStr);
            System.out.println("Saved graph successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
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