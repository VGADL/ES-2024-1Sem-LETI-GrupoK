package iscteiul.ista.gestaoterritorio.test;

import iscteiul.ista.gestaoterritorio.GraphProprietario;
import iscteiul.ista.gestaoterritorio.PolygonList;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphProprietarioTest {

    private GraphProprietario graphProprietario;
    private PolygonList polygonList;

    @BeforeEach
    void setUp() {
        graphProprietario = new GraphProprietario();
        polygonList = new PolygonList();

        // Mock atualizado de polígonos
        Map<String, String> polygons = new LinkedHashMap<>();
        polygons.put("T1_1", "POINT(0 0)");
        polygons.put("T2_1", "POINT(1 1)");
        polygons.put("T1_2", "POINT(0 1)");
        polygons.put("T3_1", "POINT(2 2)");
        polygonList.setPolygons(polygons);
    }

    @Test
    void getGraph() {
        Graph<String, DefaultEdge> graph = graphProprietario.getGraph();
        assertNotNull(graph, "O grafo retornado não deve ser nulo.");
        assertTrue(graph.vertexSet().isEmpty(), "O grafo inicial deve estar vazio.");
    }

    @Test
    void buildGraph() {
        graphProprietario.buildGraph(polygonList);

        Graph<String, DefaultEdge> graph = graphProprietario.getGraph();
        assertNotNull(graph, "O grafo retornado não deve ser nulo.");

        // Verifica se há vértices
        System.out.println("Vértices no grafo: " + graph.vertexSet());
        assertFalse(graph.vertexSet().isEmpty(), "O grafo deve conter vértices após a construção.");

        // Verifica se há arestas
        System.out.println("Arestas no grafo: " + graph.edgeSet());
        assertFalse(graph.edgeSet().isEmpty(), "O grafo deve conter arestas após a construção.");

        // Verifica vértices específicos
        assertTrue(graph.vertexSet().contains("Owner_T1"), "Owner_T1 deve estar no grafo.");
        assertTrue(graph.vertexSet().contains("Owner_T2"), "Owner_T2 deve estar no grafo.");

        // Verifica arestas específicas (se aplicável)
        assertTrue(graph.containsEdge("Owner_T1", "Owner_T2"), "Deve haver uma aresta entre Owner_T1 e Owner_T2.");
    }

    @Test
    void getProprietario() {
        // Simula um ficheiro CSV para teste
        String mockFilePath = "src/test/resources/mockTerrenos.csv";

        // Testa um ID existente no CSV
        String expectedOwner = "Owner_T1";
        String owner = graphProprietario.getProprietario("T1", mockFilePath);
        assertEquals(expectedOwner, owner, "O proprietário retornado não corresponde ao esperado.");

        // Testa um ID inexistente
        String nonExistentOwner = graphProprietario.getProprietario("T99", mockFilePath);
        assertNull(nonExistentOwner, "O método deve retornar null para um ID inexistente.");
    }
}