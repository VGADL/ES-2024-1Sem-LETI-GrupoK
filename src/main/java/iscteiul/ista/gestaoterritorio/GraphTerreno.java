package iscteiul.ista.gestaoterritorio;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.locationtech.jts.geom.Coordinate;

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
                    if (areTerrainsAdjacent(terrainPolygons.get(terrainId1), terrainPolygons.get(terrainId2), polygons)) {
                        graph.addEdge(terrainId1, terrainId2);
                    }
                }
            }
        }
    }

    /**
     * Verifica se dois terrenos são adjacentes.
     *
     * @param polygons1 Lista de polígonos do primeiro terreno.
     * @param polygons2 Lista de polígonos do segundo terreno.
     * @param allPolygons Mapa com todos os polígonos processados.
     * @return true se os terrenos são adjacentes, false caso contrário.
     */
    private boolean areTerrainsAdjacent(List<String> polygons1, List<String> polygons2, Map<String, String> allPolygons) {
        for (String polygonId1 : polygons1) {
            for (String polygonId2 : polygons2) {
                if (arePolygonsAdjacent(allPolygons.get(polygonId1), allPolygons.get(polygonId2))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica se dois polígonos são adjacentes.
     *
     * @param polygon1 Representação WKT do primeiro polígono.
     * @param polygon2 Representação WKT do segundo polígono.
     * @return true se os polígonos são adjacentes, false caso contrário.
     */
    public boolean arePolygonsAdjacent(String polygon1, String polygon2) {
        List<Coordinate> coordinates1 = extractCoordinates(polygon1);
        List<Coordinate> coordinates2 = extractCoordinates(polygon2);

        for (Coordinate coord1 : coordinates1) {
            for (Coordinate coord2 : coordinates2) {
                if (areCoordinatesAdjacent(coord1, coord2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica se duas coordenadas são adjacentes com base numa tolerância de distância.
     *
     * @param coord1 Primeira coordenada.
     * @param coord2 Segunda coordenada.
     * @return true se as coordenadas são adjacentes, false caso contrário.
     */
    private boolean areCoordinatesAdjacent(Coordinate coord1, Coordinate coord2) {
        // Define uma tolerância para considerar como "adjacente".
        double tolerance = 0.001;
        return Math.hypot(coord1.x - coord2.x, coord1.y - coord2.y) <= tolerance;
    }

    /**
     * Extrai uma lista de coordenadas de uma representação WKT de um polígono.
     *
     * @param polygon Representação WKT do polígono (ex.: "POLYGON ((...))").
     * @return Lista de coordenadas extraídas do polígono.
     */
    private List<Coordinate> extractCoordinates(String polygon) {
        List<Coordinate> coordinates = new ArrayList<>();

        // Remove os delimitadores POLYGON (( e os parênteses extras
        String coordinateString = polygon.replace("POLYGON ((", "")
                .replace("))", "")
                .replace("(", "")
                .replace(")", ""); // Remove parênteses residuais

        // Divide as coordenadas pelo separador de pontos ", "
        String[] points = coordinateString.split(", ");
        for (String point : points) {
            String[] xy = point.trim().split("\\s+"); // Divide por espaços em branco (pode haver múltiplos espaços)
            if (xy.length == 2) { // Garante que há dois valores (x, y)
                try {
                    double x = Double.parseDouble(xy[0]);
                    double y = Double.parseDouble(xy[1]);
                    coordinates.add(new Coordinate(x, y));
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao processar coordenada: " + Arrays.toString(xy));
                }
            } else {
                System.err.println("Formato inválido para o ponto: " + point);
            }
        }
        return coordinates;
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
