package iscteiul.ista.gestaoterritorio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jgrapht.graph.DefaultEdge;

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
    @FXML
    private TextField proprietarioTextField;
    @FXML
    private ListView<String> territoriosListView;
    @FXML
    private TextArea suggestionTextArea; // Mostra as sugestões de troca
    private CsvReader csvReader;
    private AreaCalculator areaCalculator;
    private String currentAdministrativeUnitType;
    private GraphTerreno graphTerreno;
    private File selectedFile;
    private Boolean fileSelected = false;
    private GraphProprietario graphProprietario; // Grafo de proprietários
    private PropertiesExchangeSugestion exchangeSugestion; // Sugestões de troca

    /**
     * Método para selecionar e carregar um arquivo CSV.
     */
    @FXML
    private void selecionarFicheiro() {
        // Implementação da seleção de arquivo e construção dos dados.
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
                areaCalculator = new AreaCalculator();
                PolygonList polygonList = new PolygonList();
                polygonList.processRecords(csvReader.getRecords());
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
                // Constrói o grafo de terrenos
                graphTerreno = new GraphTerreno(); // Nome da classe corrigido
                graphTerreno.buildGraph(polygonList);
                // Reduz polygonList para apenas os 10 primeiros polígonos
                Map<String, String> originPolygon = polygonList.getPolygons();
                Map<String, String> reducedPolygon = new LinkedHashMap<>();
                int num = 0;
                for (Map.Entry<String, String> entry : originPolygon.entrySet()) {
                    if (num >= 15) break;
                    reducedPolygon.put(entry.getKey(), entry.getValue());
                    num++;
                }
                polygonList.setPolygons(reducedPolygon);
                graphProprietario = new GraphProprietario();
                // Constrói os grafos com base na polygonList
                graphProprietario.buildGraph(polygonList);
                // Exibe uma mensagem de sucesso
                mostrarAlerta("Sucesso", "Ficheiro Carregado", "O ficheiro foi carregado com sucesso.", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                // Lida com erros na leitura do ficheiro
                mostrarAlerta("Erro", "Erro ao carregar ficheiro", e.getMessage(), Alert.AlertType.ERROR);
            }
            if(!fileSelected) {
                AppGUI.mudarScene(1);
                fileSelected = true;
            }
            System.out.println(selectedFile.getAbsolutePath());
        } else {
            // Caso o utilizador não selecione nenhum ficheiro
            mostrarAlerta("Aviso", "Nenhum ficheiro selecionado", "Por favor, selecione um ficheiro válido.", Alert.AlertType.WARNING);
        }
    }
    /**
     * Quatro funções para navegar entre cenas.
     *
     * @param event evento de clique.
     */
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
    /**
     * Fecha a aplicação.
     *
     * @param event evento de clique.
     */
    @FXML
    void close(ActionEvent event) {
        System.exit(0);
    }
    /**
     * Exibe todos os registros do arquivo CSV.
     */
    @FXML
    public void showAll() {
        // Implementação da exibição de todos os registros.
        List<String> headers = csvReader.getHeaders();
        listView2.getItems().clear();
        List<String> itensOrdenados = new ArrayList<>();
        for (Map<String, String> recordData : csvReader.getRecords()) {
            StringBuilder recordDisplay = new StringBuilder();
            for (String header : headers) {
                recordDisplay.append(header).append(": ").append(recordData.get(header)).append(" | ");
            }
            itensOrdenados.add(recordDisplay.toString());
        }
        listView2.getItems().addAll(itensOrdenados);
    }
    /**
     * Procura por um terreno utilizando o ID especificado.
     */
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
    /**
     * Exibe os terrenos adjacentes ao terreno especificado pelo ID.
     *
     * @param event evento de clique.
     */
    @FXML
    private void showAdjacencies(ActionEvent event) {
        listView2.getItems().clear();
        String propertyId = searchIdField.getText().trim();

        // Verificar se o campo está vazio
        if (propertyId.isEmpty()) {
            Alerta("Erro", "Campo vazio", "Por favor, insira um ID válido.", Alert.AlertType.WARNING);
            return;
        }

        // Verificar se o ID existe no CSV
        boolean idExists = csvReader.getRecords().stream()
                .anyMatch(record -> Objects.equals(record.get("OBJECTID"), propertyId));

        if (!idExists) {
            Alerta("Erro", "ID não encontrado", "O ID fornecido não existe. Por favor, insira um ID válido.", Alert.AlertType.WARNING);
            return;
        }

        // Verificar se há adjacências no grafo
        if (graphTerreno.getAdjacentTerrains(propertyId).isEmpty()) {
            listView2.getItems().add("Nenhuma adjacência encontrada para o ID: " + propertyId);
        } else {
            listView2.getItems().add("Adjacências para o ID " + propertyId + ":");
            listView2.getItems().addAll(graphTerreno.getAdjacentTerrains(propertyId));
        }
    }

    /**
     * Exibe uma mensagem de alerta na interface.
     *
     * @param title       título do alerta.
     * @param header      cabeçalho do alerta.
     * @param description descrição do alerta.
     * @param type        tipo do alerta.
     */
    public void Alerta(String title, String header, String description,Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(description);
        alert.show();
    }
    /**
     * Retorna para a cena inicial.
     *
     * @param event evento de clique.
     */
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
    /**
     * Atualiza a lista de unidades administrativas com base no tipo especificado.
     *
     * @param tipo tipo de unidade administrativa.
     */
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
    /**
     * Manipula cliques em itens da lista para calcular a área.
     *
     * @param event evento de clique.
     */
    @FXML
    private void handleItemClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Verifica duplo clique
            calculateArea();
        }
    }
    /**
     * Calcula a área média da unidade administrativa selecionada.
     */
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
    /**
     * Calcula a área média dos terrenos adjacentes ao proprietário selecionado.
     */
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

    /**
     * Procura terrenos associados ao proprietário especificado.
     */
    @FXML
    public void procurarPorProprietário() {
        String id = proprietarioTextField.getText().trim(); // Obtém o ID do proprietário

        // Verifica se o campo está vazio
        if (id.isEmpty()) {
            Alerta("Sem input", "Null String", "Coloque um proprietário de um terreno", Alert.AlertType.WARNING);
            return;
        }

        List<String> headers = csvReader.getHeaders(); // Obtém os cabeçalhos do CSV
        territoriosListView.getItems().clear(); // Limpa a lista antes de adicionar os resultados

        boolean found = false;

        // Percorre os registros do CSV
        for (Map<String, String> recordData : csvReader.getRecords()) {
            if (recordData.get("OWNER").equals(id)) { // Verifica se o ID corresponde ao proprietário
                // Monta uma string formatada com os dados do registro
                StringBuilder recordDisplay = new StringBuilder();
                for (String header : headers) {
                    recordDisplay.append(header).append(": ").append(recordData.get(header)).append(" | ");
                }
                // Adiciona o registro formatado à ListView
                territoriosListView.getItems().add(recordDisplay.toString());
                found = true;
            }
        }

        // Exibe alerta se nenhum terreno foi encontrado
        if (!found) {
            Alerta("Proprietário não encontrado", null, "Nenhum terreno associado ao proprietário especificado foi encontrado.", Alert.AlertType.ERROR);
        }
    }
    /**
     * Exibe os proprietários conectados ao proprietário especificado.
     *
     * @param event evento de clique.
     */
    @FXML
    private void showAdjacenciesProprietarios(ActionEvent event) {
        territoriosListView.getItems().clear(); // Limpa os resultados anteriores
        String owner = proprietarioTextField.getText().trim(); // Obtém o ID do proprietário

        // Verifica se o campo está vazio
        if (owner.isEmpty()) {
            Alerta("Erro", "Campo vazio", "Por favor, insira um ID válido.", Alert.AlertType.WARNING);
            return;
        }

        // Verifica se o proprietário existe no grafo
        if (!graphProprietario.getGraph().containsVertex(owner)) {
            territoriosListView.getItems().add("O proprietário " + owner + " não existe no grafo.");
            return;
        }

        // Obtém os IDs dos proprietários conectados (adjacentes)
        Set<String> connectedOwners = new HashSet<>();
        for (DefaultEdge edge : graphProprietario.getGraph().edgesOf(owner)) {
            String source = graphProprietario.getGraph().getEdgeSource(edge);
            String target = graphProprietario.getGraph().getEdgeTarget(edge);

            // Adiciona o vértice conectado que não seja o próprio proprietário
            connectedOwners.add(source.equals(owner) ? target : source);
        }

        // Verifica se há conexões
        if (connectedOwners.isEmpty()) {
            territoriosListView.getItems().add("Nenhuma adjacência encontrada para o proprietário " + owner + ".");
        } else {
            territoriosListView.getItems().add("Adjacências para o proprietário " + owner + ":");
            territoriosListView.getItems().addAll(connectedOwners);
        }
    }

    /**
     * Gera e exibe sugestões de troca de propriedades.
     */
    @FXML
    private void generateSuggestions() {
        if (csvReader == null || graphTerreno == null || graphProprietario == null) {
            mostrarAlerta("Erro", "Dados ausentes", "Certifique-se de carregar o CSV e construir os grafos antes de gerar sugestões.", Alert.AlertType.ERROR);
            return;
        }
        exchangeSugestion = new PropertiesExchangeSugestion();
        List<PropertiesExchangeSugestion.ExchangeSuggestion> suggestions =
                exchangeSugestion.generateExchangeSuggestions(csvReader, graphTerreno, graphProprietario);
        displaySuggestions(suggestions);
    }
    private void displaySuggestions(List<PropertiesExchangeSugestion.ExchangeSuggestion> suggestions) {
        StringBuilder sb = new StringBuilder();
        for (PropertiesExchangeSugestion.ExchangeSuggestion suggestion : suggestions) {
            sb.append(suggestion.toString()).append("\n");
        }
        suggestionTextArea.setText(sb.toString());
    }

}
