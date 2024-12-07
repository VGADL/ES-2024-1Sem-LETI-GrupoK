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

    /**
     * Método recursivo para calcular a área combinada de um terreno e seus adjacentes
     * pertencentes ao mesmo proprietário.
     *
     * @param terrainId ID do terreno atual.
     * @param owner Nome do proprietário.
     * @param graph Grafo que contém os terrenos e suas adjacências.
     * @param records Lista de registos do CSV.
     * @param visited Conjunto de terrenos já visitados.
     * @param graphTerreno Objeto {@link GraphTerreno} para obter as adjacências.
     * @return A área total combinada do terreno e seus adjacentes pertencentes ao mesmo proprietário.
     */
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

}
