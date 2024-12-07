package iscteiul.ista.gestaoterritorio.test;

import iscteiul.ista.gestaoterritorio.AreaCalculator;
import iscteiul.ista.gestaoterritorio.CsvReader;
import iscteiul.ista.gestaoterritorio.GraphTerreno;
import iscteiul.ista.gestaoterritorio.PolygonList;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AreaCalculatorTest {

    private AreaCalculator areaCalculator;
    private CsvReader mockCsvReader;
    private GraphTerreno mockGraphTerreno;

    @BeforeEach
    void setUp() {
        try {
            areaCalculator = new AreaCalculator();

            String filePath = "src/main/ficheiros/Madeira-Moodle-1.1.csv";
            String delimiter = ";";
            mockCsvReader = new CsvReader(filePath, delimiter);
            mockCsvReader.readFile();

            // Instanciar a lista de polígonos e processar os registos do CSV
            PolygonList polygonList = new PolygonList();
            polygonList.processRecords(mockCsvReader.getRecords());

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
            mockGraphTerreno = new GraphTerreno();
            mockGraphTerreno.buildGraph(polygonList);
        } catch (Exception e) {
            System.err.println("Erro ao processar os dados:");
            e.printStackTrace();
        }
    }

    @Test
    void calculateAverageAreaByAdministrativeUnit() {
        // Simula registros CSV
        List<Map<String, String>> records = List.of(
                Map.of("Shape_Area", "100.0", "Freguesia", "Arco da Calheta"),
                Map.of("Shape_Area", "200.0", "Freguesia", "Arco da Calheta"),
                Map.of("Shape_Area", "150.0", "Freguesia", "Calheta")
        );

        mockCsvReader.setRecords(records);

        // Testa o cálculo para a unidade administrativa "Arco da Calheta"
        double result = areaCalculator.calculateAverageAreaByAdministrativeUnit(
                "Freguesia", "Arco da Calheta", mockCsvReader);

        assertEquals(150.0, result, 0.01, "Área média calculada incorretamente.");
    }

    @Test
    void calculateAverageAreaByAdministrativeUnit_NoMatchingRecords() {
        // Simula registros CSV
        List<Map<String, String>> records = List.of(
                Map.of("Shape_Area", "100.0", "Freguesia", "Outra Freguesia")
        );

        mockCsvReader.setRecords(records);

        // Testa o cálculo para uma unidade administrativa sem registros correspondentes
        double result = areaCalculator.calculateAverageAreaByAdministrativeUnit(
                "Freguesia", "Arco da Calheta", mockCsvReader);

        assertEquals(0.0, result, "A área média deve ser 0 quando não há registros correspondentes.");
    }
}