package iscteiul.ista.gestaoterritorio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolygonList {
    private final Map<String, String> polygonMap;

    public PolygonList() {
        this.polygonMap = new HashMap<>();
    }

    /**
     * Processa os registos lidos do arquivo CSV e armazena os polígonos na lista.
     *
     * @param records Lista de registos lidos pelo CsvReader.
     */
    public void processRecords(List<Map<String, String>> records) {
        for (Map<String, String> record : records) {
            String id = record.get("OBJECTID"); // Pega o ID do registo
            String geometry = record.get("geometry"); // Pega o campo 'geometry'

            if (geometry != null && geometry.startsWith("MULTIPOLYGON")) {
                processMultipolygon(id, geometry);
            }
        }
    }

    /**
     * Processa um multipolígono e divide os polígonos individuais.
     *
     * @param id       O ID do registo.
     * @param geometry A string contendo o multipolígono.
     */
    private void processMultipolygon(String id, String geometry) {
        // Remove o prefixo MULTIPOLYGON (( e os parênteses extras no final
        String multipolygonData = geometry
                .replace("MULTIPOLYGON (((", "")
                .replace(")))", "");

        // Divide os polígonos pelo "), (" que separa diferentes polígonos no multipolígono
        String[] polygons = multipolygonData.split("\\), \\(");

        // Adiciona cada polígono ao mapa com um ID único (ex: "105_1", "105_2")
        for (int i = 0; i < polygons.length; i++) {
            String polygonId = id + "_" + (i + 1);
            String polygonData = "POLYGON (((" + polygons[i] + ")))"; // Reconstrói o formato completo
            polygonMap.put(polygonId, polygonData);
        }
    }

    /**
     * Retorna todos os polígonos processados num mapa.
     *
     * @return Um mapa contendo os IDs e os respectivos polígonos.
     */
    public Map<String, String> getPolygons() {
        return polygonMap;
    }

    public static void main(String[] args) {
        try {
            // Instancia o CsvReader
            CsvReader csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
            csvReader.readFile();

            // Processa os registos e armazena os polígonos
            PolygonList polygonList = new PolygonList();
            polygonList.processRecords(csvReader.getRecords());

            // Imprime os polígonos processados
            for (Map.Entry<String, String> entry : polygonList.getPolygons().entrySet()) {
                System.out.println("ID: " + entry.getKey() + " -> " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

