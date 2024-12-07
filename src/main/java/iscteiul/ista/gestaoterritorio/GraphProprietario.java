package iscteiul.ista.gestaoterritorio;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Classe responsável por construir e manipular um grafo que representa terrenos e as suas adjacências.
 * Cada vértice no grafo representa um terreno, e as arestas indicam que os terrenos são adjacentes.
 */
public class GraphProprietario {
    private final Graph<String, DefaultEdge> graph;

    /**
     * Construtor da classe GraphTerreno.
     * Inicializa um grafo simples sem loops e arestas múltiplas.
     */
    public GraphProprietario() {
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
     * Constrói o grafo de proprietários a partir da lista de polígonos.
     *
     * @param polygonList Instância da classe PolygonList contendo os polígonos.
     */
    public void buildGraph(PolygonList polygonList) {
        String filePath = "src/main/ficheiros/Madeira-Moodle-1.1.csv";

        Map<String, String> polygons = polygonList.getPolygons();

        // Agrupa polígonos por terreno
        Map<String, List<String>> terrainPolygons = new LinkedHashMap<>();
        for (String polygonId : polygons.keySet()) {
            String terrainId = polygonId.split("_")[0];
            terrainPolygons.computeIfAbsent(terrainId, k -> new ArrayList<>()).add(polygonId);
        }

        // Adiciona proprietários como vértices
        Set<String> owners = new LinkedHashSet<>();
        for (String terrainId : terrainPolygons.keySet()) {
            String owner = getProprietario(terrainId, filePath);
            if (owner != null) {
                owners.add(owner);
            }
        }
        owners.forEach(graph::addVertex);

        // Constrói as arestas do grafo (relação de vizinhança entre proprietários)
        for (String terrainId1 : terrainPolygons.keySet()) {
            for (String terrainId2 : terrainPolygons.keySet()) {
                if (!terrainId1.equals(terrainId2)) {
                    boolean areAdjacent = TerrainProperties.areTerrainsAdjacent(
                            terrainPolygons.get(terrainId1),
                            terrainPolygons.get(terrainId2),
                            polygons
                    );

                    String owner1 = getProprietario(terrainId1, filePath);
                    String owner2 = getProprietario(terrainId2, filePath);

                    if (areAdjacent && owner1 != null && owner2 != null && !owner1.equals(owner2)) {
                        graph.addEdge(owner1, owner2);
                    }
                }
            }
        }
    }

    /**
     * Retorna o ID do proprietário (OWNER) associado a um determinado terreno (OBJECTID).
     *
     * @param terrenoID ID do terreno (OBJECTID) no ficheiro CSV.
     * @param csvFilePath Caminho para o ficheiro CSV contendo os dados dos terrenos.
     * @return ID do proprietário (OWNER) ou null se o terreno não for encontrado.
     */
    public String getProprietario(String terrenoID, String csvFilePath) {
        try {
            // Lê o ficheiro CSV
            CsvReader csvReader = new CsvReader(csvFilePath, ";");
            csvReader.readFile();

            // Procura o terreno pelo OBJECTID e retorna o OWNER correspondente
            for (Map<String, String> record : csvReader.getRecords()) {
                if (record.get("OBJECTID").equals(terrenoID)) {
                    return record.get("OWNER");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao acessar o arquivo CSV: " + e.getMessage());
        }
        // Retorna null se o terreno não for encontrado ou ocorrer erro
        return null;
    }


//    public static void main(String[] args) {
//        try {
//            // Caminho para o ficheiro CSV
//            String filePath = "src/main/ficheiros/Madeira-Moodle-1.1.csv";
//            String delimiter = ";";
//
//            // Lê o ficheiro CSV e processa os polígonos
//            CsvReader csvReader = new CsvReader(filePath, delimiter);
//            csvReader.readFile();
//
//            PolygonList polygonList = new PolygonList();
//            polygonList.processRecords(csvReader.getRecords());
//
//            // Reduz polygonList para apenas os 100 primeiros polígonos
//            Map<String, String> originalPolygons = polygonList.getPolygons();
//            Map<String, String> reducedPolygons = new LinkedHashMap<>();
//
//            int count = 0;
//            for (Map.Entry<String, String> entry : originalPolygons.entrySet()) {
//                if (count >= 15) break;
//                reducedPolygons.put(entry.getKey(), entry.getValue());
//                count++;
//            }
//
//            polygonList.setPolygons(reducedPolygons);
//
//            // Constrói o grafo de proprietários
//            GraphProprietario proprietarioGraph = new GraphProprietario();
//            proprietarioGraph.buildGraph(polygonList);
//
//            // Log do grafo para validação
//            System.out.println("\nResumo do grafo de proprietários:");
//            proprietarioGraph.getGraph().vertexSet().forEach(owner -> {
//                Set<String> connectedOwners = new HashSet<>();
//                for (DefaultEdge edge : proprietarioGraph.getGraph().edgesOf(owner)) {
//                    String source = proprietarioGraph.getGraph().getEdgeSource(edge);
//                    String target = proprietarioGraph.getGraph().getEdgeTarget(edge);
//                    connectedOwners.add(source.equals(owner) ? target : source);
//                }
//                System.out.println("Proprietário " + owner + " está conectado a: " + connectedOwners);
//            });
//
//        } catch (Exception e) {
//            System.err.println("Ocorreu um erro ao executar o programa:");
//            e.printStackTrace();
//        }
//    }


}
