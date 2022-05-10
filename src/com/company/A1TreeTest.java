package com.company;

/**
 * Tests für die erste Praktikumsaufgabe GKAP
 * SoSe2022
 * @author: Gerhard Oelker
 */

import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.view.Viewer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

// Die Methoden, die Sie schreiben sollen:
//public static Graph generateTree(int n);
//public static Graph fromPrueferCodeToTree(long[] code);
//public static long[] fromTreeToPrueferCode(Graph g);

//import static gkap.aufgabe1.PrueferTreeAlgorithms.*;
import static com.company.GraphMethods.*;


public class A1TreeTest {
    public static final int NUMFILES = 8;  //hängt ab, von der Anzahl an Bsp.
    public static final int TESTRUNS = 10; //Vorschlag, darf auch mehr sein
    public static final int MAXCODELENGTH = 1042; // was sonst?

    private List<Graph> trees = new ArrayList<>();

    /**
     * Ausgelagerte Testmethode; wird an unterschiedlichen Stellen gebraucht
     * Überprüft ob der übergebene Graph ein Baum ist
     * @param g
     */
    private void checkIsTree(Graph g) {
        int nodesCounted = 0; //Knoten einer Komponente
        //Wenn zusammenhängend, findet BFS alle Knoten des Graphen
        BreadthFirstIterator bfi = new BreadthFirstIterator(g.getNode(0));

        while(bfi.hasNext()) {
            Node node = bfi.next();
            nodesCounted++;
        }
        int nodeCount = g.getNodeCount(); //alle Knoten im Graph
        assertEquals("Nicht zusammenhängend!", nodeCount, nodesCounted);
        assertEquals("Nicht kreisfrei!", nodeCount-1, g.edges().count());
    }

    /**
     * Baut aus einer edge eine zwei-elementige Menge, deren
     * Elemente die Labels der inzidenten Knoten sind.
     * @param e
     * @return
     */
    private static Set tupleOfNodeLabels(Edge e) {
        Set hs = new HashSet();
        //Die Labels aus den .dot-Dateien sind Strings
        hs.add((String) e.getSourceNode().getAttribute("label"));
        hs.add((String) e.getTargetNode().getAttribute("label"));
        return hs;
    }

    /**
     * Generiert einen Prüfer-Code mit dem Zahlenbereich von
     * 1 - numNodes (inklusive)
     * @param numNodes
     * @return Array mit dem Code
     */
    private static long[] randomlyGeneratedPrueferCode(int numNodes) {
        //Der Prüfercode hat zwei Stellen weniger als
        //Knoten im resultierenden Graph sind
        var length = numNodes - 2;
        var result = new long[length];
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            result[i] = rand.nextInt(numNodes) + 1;
        }
        return result;
    }

    // Für die 'kleinen' Beispiele
    // Liest die .dot-Files ein und speichert die Graphen in einer Liste
    @Before
    public void readTrees() {
        for (int i = 1; i <= NUMFILES; i++) {
            //die .dot-files liegen bei mir im Projekt unter data
            //ggf. anpassen
            String fileName = String.format("data/BT%s.dot", i);
            trees.add(fromFile(fileName));
        }
    }

    /**
     * Vergleicht zwei Bäume
     * @param tree1
     * @param tree2
     * @return
     */
    public static boolean treeEquals(Graph tree1, Graph tree2) {
        Set set1 = tree1.edges().
                map(A1TreeTest::tupleOfNodeLabels).
                collect(Collectors.toSet());
        Set set2 = tree2.edges().
                map(A1TreeTest::tupleOfNodeLabels).
                collect(Collectors.toSet());
        return set1.equals(set2);
    }

    /**
     * Liest eine .dot-Datei und gibt den zugehörigen Graph zurück
     * nach dem Tutorial von Graphstream
     * @param filePath
     * @return
     */
    public static Graph fromFile(String filePath) {

        Graph g = new SingleGraph("g");
        FileSource fs = null;
        try {
            fs = FileSourceFactory.sourceFor(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fs.removeSink(g);
        }
        fs.addSink(g);
        try {
            fs.readAll(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return g;
    }

    // Testet, ob die mitgelieferten Graphen Bäume sind
    @Test
    public void testIsTreeExamples() {
        for (Graph g:trees) {
            checkIsTree(g);
        }
    }

    // Testet, ob ein generierter Graph ein Baum ist
    @Test
    public void testIsTreeGenerated() {
        Random rand = new Random();

        for (int i = 0; i < TESTRUNS; i++) {
            Graph g = generateTree(rand.nextInt(MAXCODELENGTH) + 2);
            checkIsTree(g);
        }
    }

    //Test basierend auf den Beispielen
    @Test
    public void testOfPrueferTree() {

        for (Graph g:trees) {
            long[] expected = Arrays.stream(((String) g.getAttribute("code")).
                            split(",")).
                    mapToLong(Long::valueOf).toArray();
            checkIsTree(g);
            assertArrayEquals(expected, fromTreeToPrueferCode(g));
        }
    }

    //Test basierend auf den Beispielen
    @Test
    public void testOfPrueferCode() {

        for (Graph g:trees) {
            long[] code = Arrays.stream(((String) g.getAttribute("code")).split(",")).
                    mapToLong(Long::valueOf).toArray();
            Graph calculated = fromPrueferCodeToTree(code);
            checkIsTree(calculated);
            assert(treeEquals(g,calculated));
        }
    }

    // randomisierter Test
    @Test
    public void testTreeCodeTree() {

        for (int i = 0; i < TESTRUNS; i++) {
            int[] nodeCounts = {1,2,10,50,100,500,1000,200,500,1000};
            Graph g1 = generateTree(nodeCounts[i]);
            checkIsTree(g1);
            var code = fromTreeToPrueferCode(g1);
            Graph g2 = fromPrueferCodeToTree(code);
            checkIsTree(g2);
            assert(treeEquals(g1,g2));
        }

    }

    // randomisierter Test
    @Test
    public void testCodeTreeCode() {

        Random rand = new Random();

        for (int i = 0; i < TESTRUNS; i++) {
            var expected = randomlyGeneratedPrueferCode(rand.nextInt(MAXCODELENGTH) + 2);
            Graph g = fromPrueferCodeToTree(expected);
            assertArrayEquals(expected, fromTreeToPrueferCode(g));
        }
    }

    // Beispiel aus Tutorial: Graph einlesen aus Datei und darstellen
    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        Graph g = fromFile("data/G2.dot");
        g.setStrict(false);
        g.setAutoCreate(true);

        for (Node node : g) {
            // holt das Label (definiert in .dot-Files) und beschriftet die Knoten in der Darstellung
            node.setAttribute("ui.label", node.getAttribute("label"));
            //node.setAttribute("ui.label", node.getId());
            //node.setAttribute("ui.label", "Knoten");
        }
        Viewer viewer = g.display();
    }
}
