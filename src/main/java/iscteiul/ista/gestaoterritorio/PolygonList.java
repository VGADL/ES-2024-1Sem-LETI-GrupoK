package iscteiul.ista.gestaoterritorio;

import java.util.*;

public class PolygonList {
    private Map<String, String> polygonMap;

    public PolygonList() {
        this.polygonMap = new HashMap<>();
    }

    public void setPolygons(Map<String, String> polygons) {
        polygonMap = polygons;
    }

    /**
     * Processa os registos lidos do ficheiro CSV e armazena os polígonos na lista.
     *
     * @param records Lista de registos lidos pelo CsvReader.
     */
    public void processRecords(List<Map<String, String>> records) {
        for (Map<String, String> record : records) {
            String id = record.get("OBJECTID"); // Vai buscar o ID do registo
            String geometry = record.get("geometry"); // Vai buscar o campo 'geometry'

            if (geometry != null && geometry.startsWith("MULTIPOLYGON")) {
                processMultipolygon(id, geometry);
            }
        }

        // Ordena o mapa de polígonos por ID
        polygonMap = sortPolygonMap(polygonMap);
    }

    /**
     * Processa um multipolígono e divide os polígonos individuais.
     *
     * @param id       O ID do registo.
     * @param geometry A string que contém o multipolígono.
     */
    private void processMultipolygon(String id, String geometry) {
        // Verifica e ignora polígonos vazios
        if (geometry.equals("MULTIPOLYGON EMPTY")) {
            return;
        }

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
     * Ordena o mapa de polígonos por ID numericamente com base no valor de x.
     *
     * @param unsortedMap O mapa não ordenado.
     * @return Um mapa ordenado numericamente por x.
     */
    private Map<String, String> sortPolygonMap(Map<String, String> unsortedMap) {
        // Cria uma lista de entradas do mapa
        List<Map.Entry<String, String>> entries = new ArrayList<>(unsortedMap.entrySet());

        // Ordena as entradas com base no valor numérico de x
        entries.sort(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().split("_")[0])));

        // Adiciona as entradas ordenadas a um LinkedHashMap
        Map<String, String> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }


    /**
     * Retorna todos os polígonos processados num mapa.
     *
     * @return Um mapa que contém os IDs e os respectivos polígonos.
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

            // Devolve os polígonos processados
            for (Map.Entry<String, String> entry : polygonList.getPolygons().entrySet()) {
                System.out.println("ID: " + entry.getKey() + " -> " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

