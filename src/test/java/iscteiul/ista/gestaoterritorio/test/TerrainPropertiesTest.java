package iscteiul.ista.gestaoterritorio.test;

import iscteiul.ista.gestaoterritorio.TerrainProperties;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TerrainPropertiesTest {

    @Test
    void testAreTerrainsAdjacent_WhenAdjacent_ShouldReturnTrue() {
        // Mock data
        List<String> polygons1 = List.of("polygon1");
        List<String> polygons2 = List.of("polygon2");
        Map<String, String> allPolygons = Map.of(
                "polygon1", "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))",
                "polygon2", "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))"
        );

        boolean result = TerrainProperties.areTerrainsAdjacent(polygons1, polygons2, allPolygons);

        assertTrue(result, "Terrains sharing adjacent polygons should be considered adjacent.");
    }

    @Test
    void testAreTerrainsAdjacent_WhenNotAdjacent_ShouldReturnFalse() {
        // Mock data
        List<String> polygons1 = List.of("polygon1");
        List<String> polygons2 = List.of("polygon3");
        Map<String, String> allPolygons = Map.of(
                "polygon1", "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))",
                "polygon3", "POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2))"
        );

        boolean result = TerrainProperties.areTerrainsAdjacent(polygons1, polygons2, allPolygons);

        assertFalse(result, "Terrains with no adjacent polygons should not be considered adjacent.");
    }

    @Test
    void testArePolygonsAdjacent_WhenAdjacent_ShouldReturnTrue() {
        String polygon1 = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        String polygon2 = "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))";

        boolean result = TerrainProperties.arePolygonsAdjacent(polygon1, polygon2);

        assertTrue(result, "Polygons sharing edges or vertices should be considered adjacent.");
    }

    @Test
    void testArePolygonsAdjacent_WhenNotAdjacent_ShouldReturnFalse() {
        String polygon1 = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";
        String polygon2 = "POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2))";

        boolean result = TerrainProperties.arePolygonsAdjacent(polygon1, polygon2);

        assertFalse(result, "Polygons with no shared edges or vertices should not be considered adjacent.");
    }

    @Test
    void testAreCoordinatesAdjacent_WhenWithinTolerance_ShouldReturnTrue() {
        Coordinate coord1 = new Coordinate(0, 0);
        Coordinate coord2 = new Coordinate(0.0005, 0.0005);

        boolean result = TerrainProperties.areCoordinatesAdjacent(coord1, coord2);

        assertTrue(result, "Coordinates within the tolerance range should be considered adjacent.");
    }

    @Test
    void testAreCoordinatesAdjacent_WhenOutsideTolerance_ShouldReturnFalse() {
        Coordinate coord1 = new Coordinate(0, 0);
        Coordinate coord2 = new Coordinate(0.01, 0.01);

        boolean result = TerrainProperties.areCoordinatesAdjacent(coord1, coord2);

        assertFalse(result, "Coordinates outside the tolerance range should not be considered adjacent.");
    }

    @Test
    void testExtractCoordinates_WhenValidPolygon_ShouldReturnCoordinates() {
        String polygon = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))";

        List<Coordinate> coordinates = TerrainProperties.extractCoordinates(polygon);

        assertNotNull(coordinates, "Extracted coordinates should not be null.");
        assertEquals(5, coordinates.size(), "Polygon should have 5 coordinates (including the closing point).");
        assertEquals(new Coordinate(0, 0), coordinates.get(0), "First coordinate should match the polygon definition.");
    }

    @Test
    void testExtractCoordinates_WhenInvalidPolygon_ShouldReturnEmptyList() {
        String polygon = "INVALID POLYGON DATA";

        List<Coordinate> coordinates = TerrainProperties.extractCoordinates(polygon);

        assertNotNull(coordinates, "Extracted coordinates should not be null.");
        assertTrue(coordinates.isEmpty(), "Invalid polygon data should result in an empty coordinate list.");
    }
}