package iscteiul.ista.gestaoterritorio.test;

import iscteiul.ista.gestaoterritorio.PolygonList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PolygonListTest {

    private PolygonList polygonList;

    @BeforeEach
    void setUp() {
        polygonList = new PolygonList();
    }

    @Test
    void setPolygons() {
        Map<String, String> polygons = new HashMap<>();
        polygons.put("1_1", "POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))");
        polygons.put("2_1", "POLYGON ((1 0, 1 1, 2 1, 2 0, 1 0))");

        polygonList.setPolygons(polygons);

        assertEquals(polygons, polygonList.getPolygons(), "Os polígonos devem ser corretamente configurados.");
    }

    @Test
    void processRecords() {
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> record1 = new HashMap<>();
        record1.put("OBJECTID", "1");
        record1.put("geometry", "MULTIPOLYGON (((0 0, 0 1, 1 1, 1 0, 0 0)))");
        records.add(record1);

        Map<String, String> record2 = new HashMap<>();
        record2.put("OBJECTID", "2");
        record2.put("geometry", "MULTIPOLYGON (((1 0, 1 1, 2 1, 2 0, 1 0)))");
        records.add(record2);

        polygonList.processRecords(records);

        Map<String, String> processedPolygons = polygonList.getPolygons();

        assertEquals(2, processedPolygons.size(), "Devem existir dois polígonos processados.");
        assertTrue(processedPolygons.containsKey("1_1"), "O polígono 1_1 deve estar presente.");
        assertTrue(processedPolygons.containsKey("2_1"), "O polígono 2_1 deve estar presente.");
        assertEquals("POLYGON (((0 0, 0 1, 1 1, 1 0, 0 0)))", processedPolygons.get("1_1"), "O polígono 1_1 deve ter a geometria correta.");
        assertEquals("POLYGON (((1 0, 1 1, 2 1, 2 0, 1 0)))", processedPolygons.get("2_1"), "O polígono 2_1 deve ter a geometria correta.");
    }

    @Test
    void processEmptyMultipolygon() {
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> record = new HashMap<>();
        record.put("OBJECTID", "1");
        record.put("geometry", "MULTIPOLYGON EMPTY");
        records.add(record);

        polygonList.processRecords(records);

        assertTrue(polygonList.getPolygons().isEmpty(), "Não deve haver polígonos processados para um multipolígono vazio.");
    }

    @Test
    void getPolygons() {
        Map<String, String> polygons = new HashMap<>();
        polygons.put("1_1", "POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))");
        polygons.put("2_1", "POLYGON ((1 0, 1 1, 2 1, 2 0, 1 0))");

        polygonList.setPolygons(polygons);

        Map<String, String> retrievedPolygons = polygonList.getPolygons();

        assertEquals(polygons, retrievedPolygons, "Os polígonos retornados devem ser iguais aos configurados.");
    }
}