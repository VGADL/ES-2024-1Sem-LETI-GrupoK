package iscteiul.ista.gestaoterritorio;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.util.*;

public class AreaCalculator {

    /**
     * Calcula a área média das propriedades para uma área geográfica ou administrativa especificada.
     *
     * @param administrativeUnitType Tipo de unidade administrativa.
     * @param administrativeUnitName Nome da unidade administrativa (freguesia, município ou distrito).
     * @param csvReader Objeto CsvReader que contém os registos lidos do arquivo CSV.
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
     * @param csvReader Objeto CsvReader que contém os registos lidos do arquivo CSV.
     * @param graphTerreno Grafo dos terrenos ligados por adjacências.
     * @return A área média das propriedades na unidade administrativa, considerando propriedades adjacentes do mesmo proprietário como uma única propriedade.
     */
    public double calculateAverageAreaWithAdjacentProperties(String administrativeUnitType, String administrativeUnitName, CsvReader csvReader, GraphTerreno graphTerreno) {
        WKTReader reader = new WKTReader();
        Map<String, Double> ownerTotalAreaMap = new LinkedHashMap<>(); // Mapa para armazenar a área combinada dos terrenos do mesmo proprietário

        // Processar os registos CSV e construir o grafo com base nos terrenos adjacentes
        for (Map<String, String> record : csvReader.getRecords()) {
            String owner = record.get("OWNER");
            String areaStr = record.get("Shape_Area");
            String geometryStr = record.get("geometry");
            String unit = record.get(administrativeUnitType);
            String terrainId = record.get("OBJECTID"); // Identificador único do terreno

            if (unit.equalsIgnoreCase(administrativeUnitName)) {
                try {
                    double area = Double.parseDouble(areaStr);

                    // Adicionar o terreno como vértice no grafo
                    graphTerreno.getGraph().addVertex(terrainId);

                    // Verificar adjacência com outros terrenos já processados
                    for (String otherTerrainId : graphTerreno.getGraph().vertexSet()) {
                        if (!terrainId.equals(otherTerrainId)) {
                            String otherGeometryStr = csvReader.getRecords().stream()
                                    .filter(r -> r.get("OBJECTID").equals(otherTerrainId))
                                    .findFirst()
                                    .map(r -> r.get("geometry"))
                                    .orElse(null);

                            if (otherGeometryStr != null && arePolygonsAdjacent(geometryStr, otherGeometryStr, reader)) {
                                graphTerreno.getGraph().addEdge(terrainId, otherTerrainId);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar o terreno: " + terrainId);
                }
            }
        }

        // Agrupar terrenos adjacentes do mesmo proprietário
        Set<String> processedTerrains = new HashSet<>();
        for (String terrainId : graphTerreno.getGraph().vertexSet()) {
            if (!processedTerrains.contains(terrainId)) {
                Set<String> connectedTerrains = new HashSet<>();
                exploreConnectedTerrains(terrainId, graphTerreno, connectedTerrains);

                double combinedArea = 0;
                String owner = "";

                for (String connectedTerrainId : connectedTerrains) {
                    processedTerrains.add(connectedTerrainId);

                    // Obter os dados do terreno conectado
                    Map<String, String> connectedRecord = csvReader.getRecords().stream()
                            .filter(r -> r.get("OBJECTID").equals(connectedTerrainId))
                            .findFirst()
                            .orElse(null);

                    if (connectedRecord != null) {
                        double area = Double.parseDouble(connectedRecord.get("Shape_Area"));
                        combinedArea += area;

                        // Considerar o mesmo proprietário
                        if (owner.isEmpty()) {
                            owner = connectedRecord.get("OWNER");
                        }
                    }
                }

                // Atualizar a área total do proprietário
                ownerTotalAreaMap.put(owner, ownerTotalAreaMap.getOrDefault(owner, 0.0) + combinedArea);
            }
        }

        // Calcular a média das áreas combinadas
        double totalArea = ownerTotalAreaMap.values().stream().mapToDouble(Double::doubleValue).sum();
        int propertyCount = ownerTotalAreaMap.size();

        return propertyCount > 0 ? totalArea / propertyCount : 0;
    }

    /**
     * Função auxiliar para explorar terrenos adjacentes recursivamente.
     *
     * @param terrainId Identificação do terreno.
     * @param graphTerreno Grafo dos terrenos ligados por adjacências.
     * @param connectedTerrains
     */
    private void exploreConnectedTerrains(String terrainId, GraphTerreno graphTerreno, Set<String> connectedTerrains) {
        connectedTerrains.add(terrainId);

        for (String adjacentTerrain : graphTerreno.getAdjacentTerrains(terrainId)) {
            if (!connectedTerrains.contains(adjacentTerrain)) {
                exploreConnectedTerrains(adjacentTerrain, graphTerreno, connectedTerrains);
            }
        }
    }

    private boolean arePolygonsAdjacent(String polygon1, String polygon2, WKTReader reader) {
        try {
            Geometry geom1 = reader.read(polygon1);
            Geometry geom2 = reader.read(polygon2);
            return geom1.touches(geom2) || geom1.intersects(geom2);
        } catch (Exception e) {
            System.err.println("Erro ao verificar adjacência: " + e.getMessage());
            return false;
        }
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
                if (count >= 100) break;
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
