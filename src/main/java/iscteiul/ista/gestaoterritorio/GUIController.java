package iscteiul.ista.gestaoterritorio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.*;

public class GUIController {
    @FXML
    private ListView<String> listView;
    @FXML
    private Label areaLabel;
    @FXML
    private TextField searchIdField;
    private CsvReader csvReader;
    private AreaCalculator areaCalculator;
    private String currentAdministrativeUnitType;
    private GraphTerreno graphTerreno;

    public void initialize() {
        try {
            csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
            csvReader.readFile();
            areaCalculator = new AreaCalculator();
//            PolygonList polygonList = new PolygonList();
//            polygonList.processRecords(csvReader.getRecords());
//            GraphTerreno terrenoGraph = new GraphTerreno();
//            terrenoGraph.buildGraph(polygonList);
//            graphTerreno = terrenoGraph;
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

            // Constrói o grafo de terrenos
            graphTerreno = new GraphTerreno(); // Nome da classe corrigido
            graphTerreno.buildGraph(polygonList);
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao carregar o CSV", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    protected void procurarTerrenoClick(ActionEvent event) {
        AppGUI.mudarScene(2);
    }

    @FXML
    protected void vizualizarGrafoClick(ActionEvent event) {
        AppGUI.mudarScene(3);
    }

    @FXML
    protected void calcularAreaClick(ActionEvent event) {
        AppGUI.mudarScene(4);
    }

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

//            CsvReader csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
//            csvReader.readFile();
//
//            PolygonList polygonList = new PolygonList();
//            polygonList.processRecords(csvReader.getRecords());
//
            // Reduz polygonList para apenas os 100 primeiros polígonos
//            Map<String, String> originalPolygons = polygonList.getPolygons();
//            Map<String, String> reducedPolygons = new LinkedHashMap<>();
//            int count = 0;
//            for (Map.Entry<String, String> entry : originalPolygons.entrySet()) {
//                if (count >= 100) break;
//                reducedPolygons.put(entry.getKey(), entry.getValue());
//                count++;
//            }
//            polygonList.setPolygons(reducedPolygons);
//            GraphTerreno terrenoGraph = new GraphTerreno();
//            terrenoGraph.buildGraph(polygonList);
            if (graphTerreno.getAdjacentTerrains(propertyId).isEmpty()) {
                listView.getItems().add("Nenhuma adjacência encontrada para o ID: " + propertyId);
            } else {
                listView.getItems().add("Adjacências para o ID " + propertyId + ":");
                listView.getItems().addAll(graphTerreno.getAdjacentTerrains(propertyId));
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


    @FXML
    private void showFreguesias() {
        atualizarLista("Freguesia");
    }

    @FXML
    private void showMunicipios() {
        atualizarLista("Municipio");
    }

    @FXML
    private void showIlhas() {
        atualizarLista("Ilha");
    }

    private void atualizarLista(String tipo) {
        currentAdministrativeUnitType = tipo; // Atualiza o tipo atual
        listView.getItems().clear();
        Set<String> itensOrdenados = new TreeSet<>();
        for (Map<String, String> record : csvReader.getRecords()) {
            String item = record.get(tipo);
            if (item != null && !item.equalsIgnoreCase("NA")) {
                itensOrdenados.add(item);
            }
        }
        listView.getItems().addAll(itensOrdenados);
    }

    @FXML
    private void handleItemClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Verifica duplo clique
            calculateArea();
        }
    }

    @FXML
    private void calculateArea() {
        String selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            mostrarAlerta("Erro", "Nenhum item selecionado", "Por favor, selecione uma entrada na lista.", Alert.AlertType.WARNING);
            return;
        }
        try {
            double averageArea = areaCalculator.calculateAverageAreaByAdministrativeUnit(
                    currentAdministrativeUnitType,
                    selectedItem,
                    csvReader
            );
            areaLabel.setText("Área média: " + averageArea + " m²");
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao calcular a área", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void calculateAreaPorProprietario() {
        String selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            mostrarAlerta("Erro", "Nenhum item selecionado", "Por favor, selecione uma entrada na lista.", Alert.AlertType.WARNING);
            return;
        }
        try {
            double averageArea = areaCalculator.calculateAverageAreaWithAdjacentProperties(
                    currentAdministrativeUnitType,
                    selectedItem,
                    csvReader,
                    graphTerreno
            );
            areaLabel.setText("Área média: " + averageArea + " m²");
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao calcular a área", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String cabecalho, String conteudo, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(conteudo);
        alerta.showAndWait();
    }
}
