package iscteiul.ista.gestaoterritorio;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A classe 'TerrainProperties' fornece métodos para manipulação e análise de terrenos,
 * especificamente focando-se na verificação de adjacência entre terrenos e polígonos.
 * Utiliza coordenadas geográficas extraídas de representações de polígonos no formato WKT (Well-Known Text)
 * para determinar se os terrenos ou polígonos são adjacentes entre si.
 */
public class TerrainProperties {

    /**
     * Verifica se dois terrenos são adjacentes.
     *
     * @param polygons1 Lista de polígonos do primeiro terreno.
     * @param polygons2 Lista de polígonos do segundo terreno.
     * @param allPolygons Mapa com todos os polígonos processados.
     * @return true se os terrenos são adjacentes, false caso contrário.
     */
    public static boolean areTerrainsAdjacent(List<String> polygons1, List<String> polygons2, Map<String, String> allPolygons) {
        for (String polygonId1 : polygons1) {
            for (String polygonId2 : polygons2) {
                if (arePolygonsAdjacent(allPolygons.get(polygonId1), allPolygons.get(polygonId2))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica se dois polígonos são adjacentes.
     *
     * @param polygon1 Representação WKT do primeiro polígono.
     * @param polygon2 Representação WKT do segundo polígono.
     * @return true se os polígonos são adjacentes, false caso contrário.
     */
    public static boolean arePolygonsAdjacent(String polygon1, String polygon2) {
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
     * Verifica se duas coordenadas são adjacentes com base numa tolerância de distância.
     *
     * @param coord1 Primeira coordenada.
     * @param coord2 Segunda coordenada.
     * @return true se as coordenadas são adjacentes, false caso contrário.
     */
    public static boolean areCoordinatesAdjacent(Coordinate coord1, Coordinate coord2) {
        // Define uma tolerância para considerar como "adjacente".
        double tolerance = 0.001;

        return coord1.distance(coord2) <= tolerance;
    }

    /**
     * Extrai uma lista de coordenadas de uma representação WKT de um polígono.
     *
     * @param polygon Representação WKT do polígono (ex.: "POLYGON ((...))").
     * @return Lista de coordenadas extraídas do polígono.
     */
    public static List<Coordinate> extractCoordinates(String polygon) {
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
}
