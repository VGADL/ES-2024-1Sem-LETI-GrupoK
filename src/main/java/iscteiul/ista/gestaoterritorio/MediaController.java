package iscteiul.ista.gestaoterritorio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MediaController {

    @FXML
    private ListView<String> listView;
    @FXML
    private Label areaLabel;

    private CsvReader csvReader;
    private AreaCalculator areaCalculator;
    private boolean showingFreguesias = true; // Flag para indicar o que está sendo mostrado

    public void initialize() {
        try {
            csvReader = new CsvReader("src/main/ficheiros/Madeira-Moodle-1.1.csv", ";");
            csvReader.readFile();
            areaCalculator = new AreaCalculator();

            showFreguesias();
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao carregar o CSV", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showFreguesias() {
        showingFreguesias = true;
        atualizarLista("Freguesia");
    }

    @FXML
    private void showMunicipios() {
        showingFreguesias = false;
        atualizarLista("Municipio");
    }
    @FXML
    private void showIlhas() {
        showingFreguesias = false;
        atualizarLista("Ilha");
    }

    private void atualizarLista(String tipo) {
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
            String administrativeUnitType = showingFreguesias ? "Freguesia" : "Municipio";
            double averageArea = areaCalculator.calculateAverageAreaByAdministrativeUnit(
                    administrativeUnitType,
                    selectedItem,
                    csvReader
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
    @FXML
    protected void goBack(ActionEvent event) {
        AppGUI.mudarScene(1);
    }
}
