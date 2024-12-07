package iscteiul.ista.gestaoterritorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GUIController extends AppGUI{
    @FXML
    private ListView<String> listView;
    @FXML
    private ListView<String> listView2;
    @FXML
    private Label areaLabel;
    @FXML
    private TextField searchIdField;
    private CsvReader csvReader;
    private AreaCalculator areaCalculator;
    private String currentAdministrativeUnitType;
    private GraphTerreno graphTerreno;
    private File selectedFile;
    public void initialize() {

    }

    @FXML
    private void selecionarFicheiro() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Ficheiro CSV");

        // Define filtros para permitir apenas ficheiros CSV
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheiros CSV", "*.csv"));

        // Abre a janela para seleção de ficheiros
        selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            try {
                // Atualiza o CsvReader com o ficheiro selecionado
                csvReader = new CsvReader(selectedFile.getAbsolutePath(), ";");
                csvReader.readFile();

                // Exibe uma mensagem de sucesso
                mostrarAlerta("Sucesso", "Ficheiro Carregado", "O ficheiro foi carregado com sucesso.", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                // Lida com erros na leitura do ficheiro
                mostrarAlerta("Erro", "Erro ao carregar ficheiro", e.getMessage(), Alert.AlertType.ERROR);
            }
            AppGUI.mudarScene(1);
            System.out.println(selectedFile.getAbsolutePath());
        } else {
            // Caso o utilizador não selecione nenhum ficheiro
            mostrarAlerta("Aviso", "Nenhum ficheiro selecionado", "Por favor, selecione um ficheiro válido.", Alert.AlertType.WARNING);
        }
    }
    public void SetPath(){

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
        // try {
            /* csvReader = new CsvReader(selectedFile.getAbsolutePath(), ";");
            csvReader.readFile(); */
        List<String> headers = csvReader.getHeaders();
        listView2.getItems().clear();
        List<String> itensOrdenados = new ArrayList<>();
        for (Map<String, String> recordData : csvReader.getRecords()) {
            StringBuilder recordDisplay = new StringBuilder();
            for (String header : headers) {
                recordDisplay.append(header).append(": ").append(recordData.get(header)).append(" | ");
            }
            itensOrdenados.add(recordDisplay.toString());
            //}
       /*  } catch (IOException e){
            System.err.println("Erro ao ler o ficheiro csv: " + e.getMessage());
       } */
        }
        listView2.getItems().addAll(itensOrdenados);
    }

    @FXML
    public void procurarPorID() {
            String id = searchIdField.getText().trim();
            if (id.isEmpty()) {
                Alerta("Sem input","Null String", "Coloque o ID de um terreno", Alert.AlertType.WARNING);
                return;
            }
            List<String> headers = csvReader.getHeaders();
            listView2.getItems().clear();  // Limpa a lista antes de adicionar o resultado da busca
            boolean found = false;
            for (Map<String, String> recordData : csvReader.getRecords()) {
                if (recordData.get("OBJECTID").equals(id)) {  // Ajuste conforme o campo de ID no CSV
                    StringBuilder recordDisplay = new StringBuilder();
                    for (String header : headers) {
                        recordDisplay.append(header).append(": ").append(recordData.get(header)).append(" | ");
                    }
                    listView2.getItems().addAll(recordDisplay.toString());
                    found = true;
                    break;
                }
            }

            if (!found) {
                Alerta("Terreno não encontrado",null, "Terreno não existe", Alert.AlertType.ERROR);
            }
    }

    @FXML
    private void showAdjacencies(ActionEvent event) {
        listView2.getItems().clear();
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
        String propertyId = searchIdField.getText().trim();
        if (propertyId.isEmpty()) {
            Alerta("Erro", "Campo vazio", "Por favor, insira um ID válido.", Alert.AlertType.WARNING);
            return;
        }
        if (graphTerreno.getAdjacentTerrains(propertyId).isEmpty()) {
            listView2.getItems().add("Nenhuma adjacência encontrada para o ID: " + propertyId);
        } else {
            listView2.getItems().add("Adjacências para o ID " + propertyId + ":");
            listView2.getItems().addAll(graphTerreno.getAdjacentTerrains(propertyId));
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
        areaCalculator = new AreaCalculator();
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
        areaCalculator = new AreaCalculator();

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
