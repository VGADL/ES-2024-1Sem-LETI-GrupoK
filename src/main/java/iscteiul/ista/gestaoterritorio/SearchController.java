package iscteiul.ista.gestaoterritorio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SearchController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private ListView<String> listView;  // ListView para exibir os registros
    @FXML
    private TextField searchIdField;
    @FXML
    private TextField searchField;
    @FXML
    private Label averageResultLabel;

    /**
     * função para fechar o programa
     * @param event
     */
    @FXML
    void close(ActionEvent event) {
        System.exit(0);
    }
    @FXML
    private TextArea textArea;
    @FXML
    private List<CsvRecord> records = new ArrayList<>();
    String filePath = "src/main/ficheiros/Madeira-Moodle-1.1.csv";
    String separador = ";";
    CsvReader csvReader = new CsvReader(filePath, separador);

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

}
