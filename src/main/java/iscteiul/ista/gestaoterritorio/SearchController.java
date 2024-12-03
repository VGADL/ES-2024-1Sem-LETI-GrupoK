package iscteiul.ista.gestaoterritorio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SearchController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField searchIdField;

    private final CsvReader csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
    private List<CsvRecord> records = new ArrayList<>();
    /**
     * função para fechar o programa
     * @param event
     */
    @FXML
    void close(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    public void showAll() {
        try {
            csvReader.readFile();
            List<String> headers = csvReader.getHeaders();
            listView.getItems().clear();
            for (Map<String, String> recordData : csvReader.getRecords()) {
                StringBuilder recordDisplay = new StringBuilder();
                for (String header : headers) {
                    recordDisplay.append(header).append(": ").append(recordData.get(header)).append(" | ");
                }
                listView.getItems().add(recordDisplay.toString());
            }
        } catch (IOException e){
            System.err.println("Erro ao ler o ficheiro csv: " + e.getMessage());
       }
}

    @FXML
    public void procurarPorID() {
        try {
            String id = searchIdField.getText().trim();
            if (id.isEmpty()) {
                Alerta("Sem input","Null String", "Coloque o ID de um terreno", Alert.AlertType.WARNING);
                return;
            }
            csvReader.readFile();
            List<String> headers = csvReader.getHeaders();
            listView.getItems().clear();  // Limpa a lista antes de adicionar o resultado da busca

            boolean found = false;
            for (Map<String, String> recordData : csvReader.getRecords()) {
                if (recordData.get("OBJECTID").equals(id)) {  // Ajuste conforme o campo de ID no CSV
                    StringBuilder recordDisplay = new StringBuilder();
                    for (String header : headers) {
                        recordDisplay.append(header).append(": ").append(recordData.get(header)).append(" | ");
                    }
                    listView.getItems().add(recordDisplay.toString());
                    found = true;
                    break;
                }
            }

            if (!found) {
                Alerta("Terreno não encontrado",null, "Terreno não existe", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {

        }
    }

    @FXML
    private void showAdjacencies(ActionEvent event) {
        listView.getItems().clear();

        String propertyId = searchIdField.getText().trim();
        if (propertyId.isEmpty()) {
            Alerta("Erro", "Campo vazio", "Por favor, insira um ID válido.", Alert.AlertType.WARNING);
            return;
        }
        try {
            CsvReader csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
            csvReader.readFile();

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
            GraphTerreno terrenoGraph = new GraphTerreno();
            terrenoGraph.buildGraph(polygonList);
            if (terrenoGraph.getAdjacentTerrains(propertyId).isEmpty()) {
                listView.getItems().add("Nenhuma adjacência encontrada para o ID: " + propertyId);
            } else {
                listView.getItems().add("Adjacências para o ID " + propertyId + ":");
                listView.getItems().addAll(terrenoGraph.getAdjacentTerrains(propertyId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
        @FXML
        private void showArea(ActionEvent event) {
            listView.getItems().clear();

            String administrativeUnit = searchIdField.getText().trim();
            if (administrativeUnit.isEmpty()) {
                Alerta("Erro", "Campo vazio", "Por favor, insira o nome da unidade administrativa (Freguesia, Município ou Distrito).", Alert.AlertType.WARNING);
                return;
            }

            try {
                // Carregar o arquivo CSV
                CsvReader csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
                csvReader.readFile();

                // Instanciar o calculador de áreas
                AreaCalculator areaCalculator = new AreaCalculator();

                // Determinar o tipo de unidade administrativa (ajustar conforme o contexto da aplicação)
                String administrativeUnitType = "Freguesia"; // Pode ser "Freguesia", "Município", ou "Distrito"

                // Calcular a área média das propriedades na unidade administrativa
                double averageArea = areaCalculator.calculateAverageAreaByAdministrativeUnit(
                        administrativeUnitType,
                        administrativeUnit,
                        csvReader
                );

                // Exibir a área média
                if (averageArea > 0) {
                    listView.getItems().add("Área média das propriedades na " + administrativeUnitType + " '" + administrativeUnit + "': " + averageArea + " m²");
                } else {
                    listView.getItems().add("Nenhuma propriedade encontrada na " + administrativeUnitType + " '" + administrativeUnit + "'.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Alerta("Erro", "Erro ao processar dados", "Ocorreu um erro ao calcular a área média. Verifique os dados e tente novamente.", Alert.AlertType.ERROR);
            }
        }

    public void Alerta(String title, String header, String description,Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(description);
        alert.show();
    }

    @FXML
    protected void goBack(ActionEvent event) {
        AppGUI.mudarScene(1);
    }
    /**
    @FXML
    public void showFreguesia(ActionEvent event) {
        try {
            csvReader.readFile();
            listView.getItems().clear();

            // Usar um TreeSet para armazenar freguesias únicas e ordenadas
            Set<String> freguesiasOrdenadas = new TreeSet<>();

            // Iterar pelos registros e adicionar as freguesias ao TreeSet
            for (Map<String, String> recordData : csvReader.getRecords()) {
                String freguesia = recordData.get("Freguesia");
                if (freguesia != null && !freguesia.equalsIgnoreCase("NA")) {
                    freguesiasOrdenadas.add(freguesia);
                }
            }

            // Adicionar as freguesias únicas e ordenadas na ListView
            listView.getItems().addAll(freguesiasOrdenadas);
        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro csv: " + e.getMessage());
        }
    }*/

}
