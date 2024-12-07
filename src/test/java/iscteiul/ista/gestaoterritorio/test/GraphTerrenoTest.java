package iscteiul.ista.gestaoterritorio.test;

import iscteiul.ista.gestaoterritorio.GraphTerreno;
import iscteiul.ista.gestaoterritorio.PolygonList;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphTerrenoTest {

    private GraphTerreno graphTerreno;
    private PolygonList polygonList;

    @BeforeEach
    void setUp() {
        graphTerreno = new GraphTerreno();
        polygonList = new PolygonList();

        // Mock data para teste
        Map<String, String> polygons = new HashMap<>();
        polygons.put("1_1", "POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))");
        polygons.put("2_1", "POLYGON ((1 0, 1 1, 2 1, 2 0, 1 0))");
        polygons.put("3_1", "POLYGON ((3 0, 3 1, 4 1, 4 0, 3 0))");
        polygonList.setPolygons(polygons);
    }

    @Test
    void getGraph() {
        assertNotNull(graphTerreno.getGraph(), "O grafo deve ser inicializado.");
        assertTrue(graphTerreno.getGraph().vertexSet().isEmpty(), "O grafo deve estar vazio inicialmente.");
    }

    @Test
    void buildGraph() {
        graphTerreno.buildGraph(polygonList);

        // Verifica os vértices esperados
        Set<String> vertices = graphTerreno.getGraph().vertexSet();
        assertTrue(vertices.contains("1"), "O vértice 1 deve existir.");
        assertTrue(vertices.contains("2"), "O vértice 2 deve existir.");
        assertTrue(vertices.contains("3"), "O vértice 3 deve existir.");

        // Verifica as arestas esperadas
        assertTrue(graphTerreno.getGraph().containsEdge("1", "2"), "Os terrenos 1 e 2 devem ser adjacentes.");
        assertFalse(graphTerreno.getGraph().containsEdge("1", "3"), "Os terrenos 1 e 3 não devem ser adjacentes.");
        assertFalse(graphTerreno.getGraph().containsEdge("2", "3"), "Os terrenos 2 e 3 não devem ser adjacentes.");
    }

    @Test
    void arePolygonsAdjacent() {
        String polygon1 = "POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))";
        String polygon2 = "POLYGON ((1 0, 1 1, 2 1, 2 0, 1 0))";
        String polygon3 = "POLYGON ((3 0, 3 1, 4 1, 4 0, 3 0))";

        assertTrue(graphTerreno.arePolygonsAdjacent(polygon1, polygon2), "Os polígonos 1 e 2 devem ser adjacentes.");
        assertFalse(graphTerreno.arePolygonsAdjacent(polygon1, polygon3), "Os polígonos 1 e 3 não devem ser adjacentes.");
    }

    @Test
    void getAdjacentTerrains() {
        graphTerreno.buildGraph(polygonList);

        Set<String> adjacentTo1 = graphTerreno.getAdjacentTerrains("1");
        assertTrue(adjacentTo1.contains("2"), "O terreno 1 deve ser adjacente ao terreno 2.");
        assertFalse(adjacentTo1.contains("3"), "O terreno 1 não deve ser adjacente ao terreno 3.");

        Set<String> adjacentTo2 = graphTerreno.getAdjacentTerrains("2");
        assertTrue(adjacentTo2.contains("1"), "O terreno 2 deve ser adjacente ao terreno 1.");
        assertFalse(adjacentTo2.contains("3"), "O terreno 2 não deve ser adjacente ao terreno 3.");
    }

    @Test
    void setGraph() {
        SimpleGraph<String, DefaultEdge> newGraph = new SimpleGraph<>(DefaultEdge.class);
        newGraph.addVertex("A");
        newGraph.addVertex("B");
        newGraph.addEdge("A", "B");

        graphTerreno.setGraph(newGraph);

        assertTrue(graphTerreno.getGraph().containsVertex("A"), "O vértice A deve ser adicionado.");
        assertTrue(graphTerreno.getGraph().containsVertex("B"), "O vértice B deve ser adicionado.");
        assertTrue(graphTerreno.getGraph().containsEdge("A", "B"), "A aresta entre A e B deve ser adicionada.");
    }
}