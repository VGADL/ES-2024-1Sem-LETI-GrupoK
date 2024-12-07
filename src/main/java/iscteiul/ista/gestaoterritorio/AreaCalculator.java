package iscteiul.ista.gestaoterritorio;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class AreaCalculator {

    /**
     * Calcula a área média das propriedades para uma área geográfica ou administrativa especificada.
     *
     * @param administrativeUnitType Tipo de unidade administrativa.
     * @param administrativeUnitName Nome da unidade administrativa (freguesia, município ou distrito).
     * @param csvReader Objeto CsvReader que contém os registos lidos do ficheiro CSV.
     * @return A área média das propriedades na unidade administrativa especificada.
     */
    public double calculateAverageAreaByAdministrativeUnit(String administrativeUnitType, String administrativeUnitName, CsvReader csvReader) {
        double totalArea = 0;
        int count = 0;

        for (Map<String, String> record : csvReader.getRecords()) {
            String area = record.get("Shape_Area");
            String unit = record.get(administrativeUnitType);

            if (unit.equalsIgnoreCase(administrativeUnitName)) {
                try {
                    totalArea += Double.parseDouble(area);
                    count++;
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao processar a área: " + area);
                }
            }
        }

        return count > 0 ? totalArea / count : 0;
    }

    /**
     * Calcula a área média das propriedades, considerando propriedades adjacentes do mesmo proprietário
     * como uma única propriedade.
     *
     * @param administrativeUnitType Tipo de unidade administrativa.
     * @param administrativeUnitName Nome da unidade administrativa (freguesia, município ou distrito).
     * @param csvReader Objeto CsvReader que contém os registos lidos do ficheiro CSV.
     * @param graphTerreno Grafo dos terrenos ligados por adjacências.
     * @return A área média das propriedades na unidade administrativa, considerando propriedades adjacentes do mesmo proprietário como uma única propriedade.
     */
    public double calculateAverageAreaWithAdjacentProperties(
            String administrativeUnitType,
            String administrativeUnitName,
            CsvReader csvReader,
            GraphTerreno graphTerreno) {

        // Map para agrupar terrenos do mesmo proprietário considerando adjacência
        Map<String, Double> groupedAreas = new HashMap<>();

        // Obtém os registos do CSV
        List<Map<String, String>> records = csvReader.getRecords();
        Graph<String, DefaultEdge> graph = graphTerreno.getGraph();

        // Filtra os terrenos pertencentes à unidade administrativa especificada
        for (Map<String, String> record : records) {
            String adminUnit = record.get(administrativeUnitType); // "Freguesia", "Municipio", etc.
            String adminName = record.get(administrativeUnitName);

            if (adminUnit.equalsIgnoreCase(administrativeUnitName)) {
                String terrainId = record.get("OBJECTID");

                // Ignorar terrenos que não estão no grafo
                if (!graph.containsVertex(terrainId)) {
                    // System.err.println("Terreno ignorado pois não está no grafo: " + terrainId);
                    continue;
                }

                String owner = record.get("OWNER");

                // Verifica os terrenos adjacentes
                if (!groupedAreas.containsKey(terrainId)) {
                    double totalArea = calculateCombinedArea(terrainId, owner, graph, records, new HashSet<>(), graphTerreno);
                    groupedAreas.put(terrainId, totalArea);
                }
            }
        }

        // Calcula a área média
        double totalGroupedArea = groupedAreas.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalGroupedArea / groupedAreas.size();
    }


    private double calculateCombinedArea(
            String terrainId,
            String owner,
            Graph<String, DefaultEdge> graph,
            List<Map<String, String>> records,
            Set<String> visited,
            GraphTerreno graphTerreno) {

        // Adiciona o terreno atual aos visitados
        visited.add(terrainId);

        // Obtém a área do terreno atual
        double totalArea = records.stream()
                .filter(record -> record.get("OBJECTID").equals(terrainId))
                .mapToDouble(record -> Double.parseDouble(record.get("Shape_Area")))
                .sum();

        // Verifica se o terreno atual existe no grafo
        if (!graph.containsVertex(terrainId)) {
            System.err.println("Terreno não encontrado no grafo: " + terrainId);
            return totalArea; // Ignorar terrenos que não estão no grafo
        }

        // Itera sobre os terrenos adjacentes
        for (String adjacentTerrainId : graphTerreno.getAdjacentTerrains(terrainId)) {
            if (!visited.contains(adjacentTerrainId) && graph.containsVertex(adjacentTerrainId)) {
                // Verifica se o proprietário é o mesmo
                String adjacentOwner = records.stream()
                        .filter(record -> record.get("OBJECTID").equals(adjacentTerrainId))
                        .map(record -> record.get("OWNER"))
                        .findFirst()
                        .orElse(null);

                if (adjacentOwner != null && adjacentOwner.equals(owner)) {
                    // Soma a área do terreno adjacente
                    totalArea += calculateCombinedArea(adjacentTerrainId, owner, graph, records, visited, graphTerreno);
                }
            }
        }

        return totalArea;
    }









    public static void main(String[] args) {
        try {
            String filePath = "src/main/ficheiros/Madeira-Moodle-1.1.csv";
            String delimiter = ";";
            CsvReader csvReader = new CsvReader(filePath, delimiter);
            csvReader.readFile();

            // Instanciar a lista de polígonos e processar os registos do CSV
            PolygonList polygonList = new PolygonList();
            polygonList.processRecords(csvReader.getRecords());

            // Reduz polygonList para apenas os 100 primeiros polígonos
            Map<String, String> originalPolygons = polygonList.getPolygons();
            Map<String, String> reducedPolygons = new LinkedHashMap<>();
            int count = 0;
            for (Map.Entry<String, String> entry : originalPolygons.entrySet()) {
                if (count >= 1110) break;
                reducedPolygons.put(entry.getKey(), entry.getValue());
                count++;
            }
            polygonList.setPolygons(reducedPolygons);

            // Construir o grafo com os polígonos reduzidos
            GraphTerreno graphTerreno = new GraphTerreno();
            graphTerreno.buildGraph(polygonList);

            AreaCalculator averageArea = new AreaCalculator();

            // Exemplos para teste
            String unidadeAdministrativa = "Freguesia";
            String nomeFreguesia = "Arco da Calheta";

            // Calcular a área média para uma freguesia específica
            double averageAreaFreguesia = averageArea.calculateAverageAreaByAdministrativeUnit(unidadeAdministrativa,nomeFreguesia, csvReader);
            System.out.println("Área média para a freguesia 'Arco da Calheta': " + averageAreaFreguesia);

            // Calcular a área média para uma freguesia específica
            double averageAreaFreguesia2 = averageArea.calculateAverageAreaWithAdjacentProperties(unidadeAdministrativa, nomeFreguesia, csvReader, graphTerreno);
            System.out.println("Área média para a " + unidadeAdministrativa + " '" + nomeFreguesia + "': " + averageAreaFreguesia2);

        } catch (Exception e) {
            System.err.println("Erro ao processar os dados:");
            e.printStackTrace();
        }
    }

}
