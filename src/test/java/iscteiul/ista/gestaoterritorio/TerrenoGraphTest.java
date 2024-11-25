package iscteiul.ista.gestaoterritorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerrenoGraphTest {

    private TerrenoGraph terrenoGraph;

    @BeforeEach
    void setUp() {
        terrenoGraph = new TerrenoGraph();
        // Adiciona terrenos fictícios ao grafo.
        terrenoGraph.addTerreno("1", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))");
        terrenoGraph.addTerreno("2", "POLYGON((1 0, 1 1, 2 1, 2 0, 1 0))");
        terrenoGraph.addTerreno("3", "POLYGON((2 0, 2 1, 3 1, 3 0, 2 0))");
        terrenoGraph.addTerreno("4", "POLYGON((0 1, 0 2, 1 2, 1 1, 0 1))");
    }

    @Test
    void testAdicionarTerreno() {
        // Verifica se o terreno foi adicionado corretamente.
        assertEquals(4, terrenoGraph.getGraph().vertexSet().size(), "Número incorreto de terrenos adicionados!");
        assertTrue(terrenoGraph.getGraph().containsVertex("1"), "Terreno 1 não foi adicionado!");
    }

    @Test
    void testConstruirAdjacencias() {
        // Construir as adjacências entre os terrenos.
        terrenoGraph.construirAdjacencias();

        // Verifica se as adjacências foram estabelecidas corretamente.
        List<String> adjacentesTerreno1 = terrenoGraph.getTerrenosAdjacentes("1");
        assertEquals(2, adjacentesTerreno1.size(), "O número de terrenos adjacentes ao Terreno 1 está incorreto!");
        assertTrue(adjacentesTerreno1.contains("2"), "Terreno 2 deveria ser adjacente ao Terreno 1!");
        assertTrue(adjacentesTerreno1.contains("4"), "Terreno 4 deveria ser adjacente ao Terreno 1!");
    }

    @Test
    void testGetTerrenosAdjacentes() {
        // Construir adjacências.
        terrenoGraph.construirAdjacencias();

        // Verifica os terrenos adjacentes ao Terreno 2.
        List<String> adjacentesTerreno2 = terrenoGraph.getTerrenosAdjacentes("2");
        assertEquals(2, adjacentesTerreno2.size(), "O número de terrenos adjacentes ao Terreno 2 está incorreto!");
        assertTrue(adjacentesTerreno2.contains("1"), "Terreno 1 deveria ser adjacente ao Terreno 2!");
        assertTrue(adjacentesTerreno2.contains("3"), "Terreno 3 deveria ser adjacente ao Terreno 2!");
    }

    @Test
    void testTerrenoSemAdjacencias() {
        // Adiciona um terreno isolado ao grafo.
        terrenoGraph.addTerreno("5", "POLYGON((10 10, 10 11, 11 11, 11 10, 10 10))");

        // Reconstrói as adjacências.
        terrenoGraph.construirAdjacencias();

        // Verifica que o terreno 5 não tem adjacências.
        List<String> adjacentesTerreno5 = terrenoGraph.getTerrenosAdjacentes("5");
        assertTrue(adjacentesTerreno5.isEmpty(), "Terreno 5 não deveria ter adjacências!");
    }

}