package iscteiul.ista.gestaoterritorio;

import org.locationtech.jts.geom.Coordinate;

import java.util.*;

public class GraphTerreno {
    private final Map<String, Set<String>> adjacencyMap;

    public GraphTerreno() {
        this.adjacencyMap = new HashMap<>();
    }

    /**
     * Constrói o grafo de terrenos a partir da lista de polígonos.
     *
     * @param polygonList Instância da classe PolygonList contendo os polígonos.
     */
    public void buildGraph(PolygonList polygonList) {
        Map<String, String> polygons = polygonList.getPolygons();

        // Map para agrupar polígonos por terreno (ex.: "105" -> [105_1, 105_2, ...])
        Map<String, List<String>> terrainPolygons = new HashMap<>();
        for (String polygonId : polygons.keySet()) {
            String terrainId = polygonId.split("_")[0];
            terrainPolygons.computeIfAbsent(terrainId, k -> new ArrayList<>()).add(polygonId);
        }

//        System.out.println("Mapa de terrenos e os seus polígonos:");
//        terrainPolygons.forEach((terrainId, polygonsInTerrain) ->
//                System.out.println("Terreno " + terrainId + ": " + polygonsInTerrain)
//        );

        // Construir as arestas do grafo consolidado por terreno
        //System.out.println("\nA iniciar a criação das arestas no grafo:");
        for (String terrainId1 : terrainPolygons.keySet()) {
            for (String terrainId2 : terrainPolygons.keySet()) {
                if (!terrainId1.equals(terrainId2)) {
                    //System.out.println("A verificar adjacência entre terreno " + terrainId1 + " e terreno " + terrainId2);
                    if (areTerrainsAdjacent(terrainPolygons.get(terrainId1), terrainPolygons.get(terrainId2), polygons)) {
                        //System.out.println("Terrenos adjacentes encontrados: " + terrainId1 + " <-> " + terrainId2);
                        addEdge(terrainId1, terrainId2);
                    } else {
                        //System.out.println("Terrenos NÃO são adjacentes: " + terrainId1 + " e " + terrainId2);
                    }
                }
            }
        }

        // Log para verificar o grafo final
        System.out.println("\nGrafo final (adjacências):");
        adjacencyMap.forEach((terrainId, neighbors) ->
                System.out.println("Terreno " + terrainId + " adjacente a: " + neighbors)
        );
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

    private boolean arePolygonsAdjacent(String polygon1, String polygon2) {
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
     * Verifica se duas coordenadas são adjacentes com base em proximidade.
     */
    private boolean areCoordinatesAdjacent(Coordinate coord1, Coordinate coord2) {
        // Define uma tolerância para considerar como "adjacente".
        double tolerance = 0.001;
        return Math.hypot(coord1.x - coord2.x, coord1.y - coord2.y) <= tolerance;
    }

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
     * Adiciona uma aresta entre dois terrenos no grafo.
     *
     * @param terrainId1 ID do primeiro terreno.
     * @param terrainId2 ID do segundo terreno.
     */
    private void addEdge(String terrainId1, String terrainId2) {
        adjacencyMap.computeIfAbsent(terrainId1, k -> new HashSet<>()).add(terrainId2);
        adjacencyMap.computeIfAbsent(terrainId2, k -> new HashSet<>()).add(terrainId1);
    }

    /**
     * Obtém os terrenos adjacentes ao terreno dado.
     *
     * @param terrainId ID do terreno.
     * @return Conjunto de IDs dos terrenos adjacentes.
     */
    public Set<String> getAdjacentTerrains(String terrainId) {
        return adjacencyMap.getOrDefault(terrainId, Collections.emptySet());
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
            System.out.println("IDs dos 100 primeiros polígonos considerados:");
            for (Map.Entry<String, String> entry : originalPolygons.entrySet()) {
                if (count >= 100) break;
                reducedPolygons.put(entry.getKey(), entry.getValue());
                System.out.println("Polígono ID: " + entry.getKey());
                count++;
            }

            polygonList.setPolygons(reducedPolygons); // Certifique-se de que existe um setter ou manipule diretamente.

            // Constrói o grafo de terrenos
            GraphTerreno terrenoGraph = new GraphTerreno();
            terrenoGraph.buildGraph(polygonList);

            // Testa o grafo com um terreno específico
            String testTerrainId = "8"; // Exemplo de terreno para teste
            System.out.println("Terrenos adjacentes ao terreno " + testTerrainId + ":");
            System.out.println(terrenoGraph.getAdjacentTerrains(testTerrainId));

        } catch (Exception e) {
            System.err.println("Ocorreu um erro ao executar o programa:");
            e.printStackTrace();
        }
    }

}

