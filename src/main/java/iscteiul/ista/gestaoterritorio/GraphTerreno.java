package iscteiul.ista.gestaoterritorio;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Classe responsável por construir e manipular um grafo que representa terrenos e as suas adjacências.
 * Cada vértice no grafo representa um terreno, e as arestas indicam que os terrenos são adjacentes.
 */
public class GraphTerreno {
    private final Graph<String, DefaultEdge> graph;

    /**
     * Construtor da classe GraphTerreno.
     * Inicializa um grafo simples sem loops e arestas múltiplas.
     */
    public GraphTerreno() {
        this.graph = new SimpleGraph<>(DefaultEdge.class);
    }

    /**
     * Retorna o grafo que representa os terrenos.
     *
     * @return o grafo de terrenos.
     */
    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }

    /**
     * Constrói o grafo de terrenos a partir da lista de polígonos.
     *
     * @param polygonList Instância da classe PolygonList contendo os polígonos.
     */
    public void buildGraph(PolygonList polygonList) {
        Map<String, String> polygons = polygonList.getPolygons();

        // Map para agrupar polígonos por terreno (ex.: "105" -> [105_1, 105_2, ...])
        Map<String, List<String>> terrainPolygons = new LinkedHashMap<>();
        for (String polygonId : polygons.keySet()) {
            String terrainId = polygonId.split("_")[0];
            terrainPolygons.computeIfAbsent(terrainId, k -> new ArrayList<>()).add(polygonId);
        }

        // Adiciona todos os terrenos como vértices no grafo
        terrainPolygons.keySet().forEach(graph::addVertex);

        // Constrói as arestas do grafo por terreno
        for (String terrainId1 : terrainPolygons.keySet()) {
            for (String terrainId2 : terrainPolygons.keySet()) {
                if (!terrainId1.equals(terrainId2)) {
                    if (TerrainProperties.areTerrainsAdjacent(terrainPolygons.get(terrainId1), terrainPolygons.get(terrainId2), polygons)) {
                        graph.addEdge(terrainId1, terrainId2);
                    }
                }
            }
        }
    }

    /**
     * Obtém os terrenos adjacentes ao terreno dado.
     *
     * @param terrainId ID do terreno.
     * @return Conjunto de IDs dos terrenos adjacentes.
     */
    public Set<String> getAdjacentTerrains(String terrainId) {
        Set<DefaultEdge> edges = graph.edgesOf(terrainId);
        Set<String> adjacentTerrains = new HashSet<>();
        for (DefaultEdge edge : edges) {
            String source = graph.getEdgeSource(edge);
            String target = graph.getEdgeTarget(edge);
            adjacentTerrains.add(source.equals(terrainId) ? target : source);
        }
        return adjacentTerrains;
    }

    public static void main(String[] args) {
        try {
            // Caminho para o ficheiro CSV
            String filePath = "src/main/ficheiros/Madeira-Moodle-1.1.csv";
            String delimiter = ";";

            // Lê o ficheiro CSV e processa os polígonos
            CsvReader csvReader = new CsvReader(filePath, delimiter);
            csvReader.readFile();

            PolygonList polygonList = new PolygonList();
            polygonList.processRecords(csvReader.getRecords());

            // Reduz polygonList para apenas os 100 primeiros polígonos
            Map<String, String> originalPolygons = polygonList.getPolygons();
            Map<String, String> reducedPolygons = new LinkedHashMap<>();

            int count = 0;
            for (Map.Entry<String, String> entry : originalPolygons.entrySet()) {
                if (count >= 100) break;
                reducedPolygons.put(entry.getKey(), entry.getValue());
                count++;
            }

            polygonList.setPolygons(reducedPolygons);

            // Constrói o grafo de terrenos
            GraphTerreno terrenoGraph = new GraphTerreno(); // Nome da classe corrigido
            terrenoGraph.buildGraph(polygonList);

            // Log do grafo para validação
            System.out.println("\nResumo do grafo:");
            terrenoGraph.getGraph().vertexSet().forEach(terrainId -> {
                System.out.println("Terreno " + terrainId + " está conectado a: " +
                        terrenoGraph.getAdjacentTerrains(terrainId));
            });

            // Testa o grafo com um terreno específico
            String testTerrainId = "8"; // Exemplo de terreno para teste
            System.out.println("\nTerrenos adjacentes ao terreno " + testTerrainId + ":");
            System.out.println(terrenoGraph.getAdjacentTerrains(testTerrainId));

        } catch (Exception e) {
            System.err.println("Ocorreu um erro ao executar o programa:");
            e.printStackTrace();
        }
    }


}
