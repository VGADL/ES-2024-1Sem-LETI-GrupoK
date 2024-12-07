package iscteiul.ista.gestaoterritorio;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvReader {
    final private String filePath;
    final private String separator;
    final private List<Map<String, String>> records = new ArrayList<>();
    final private List<String> headers = new ArrayList<>();

    /**
     * Construtor da classe CsvReader.
     *
     * @param filePath O caminho do ficheiro CSV a ser lido.
     * @param separator O separador utilizado no ficheiro CSV (ex.: vírgula, ponto e vírgula, etc.).
     */
    public CsvReader(String filePath, String separator) {
        this.filePath = filePath;
        this.separator = separator;
    }

    /**
     * Lê o ficheiro CSV e armazena os dados em memória.
     *
     * O método processa o ficheiro linha por linha. A primeira linha é considerada como o cabeçalho,
     * enquanto as linhas subsequentes são tratadas como dados. Cada linha de dados é convertida
     * em um mapa onde as chaves são os nomes das colunas e os valores são os valores dos campos.
     *
     * @throws IOException Caso ocorra um erro durante a leitura do ficheiro.
     */
    public void readFile() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Lê o cabeçalho
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headerArray = headerLine.split(separator);
                for (String header : headerArray) {
                    headers.add(header.trim());
                }
            }
            // Lê cada linha de dados
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(separator);
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String value = i < values.length ? values[i].trim() : ""; // Previne exceções se faltar uma coluna
                    record.put(headers.get(i), value);
                }
                records.add(record);
            }
        }
    }

    /**
     * Obtém a lista de registos lidos do ficheiro CSV.
     * Cada registo é representado como um mapa, onde as chaves correspondem aos nomes das colunas
     * e os valores aos dados das respetivas células.
     *
     * @return Lista de registos do ficheiro CSV.
     */
    public List<Map<String, String>> getRecords() {
        return records;
    }
    /**
     * Obtém a lista de cabeçalhos das colunas do ficheiro CSV.
     *
     * @return Lista de cabeçalhos das colunas.
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * Coloca records no CsvReader
     *
     * @param newRecords novo record.
     */
    public void setRecords(List<Map<String, String>> newRecords) {
        records.clear(); // Limpa os registos existentes
        if (newRecords != null) {
            records.addAll(newRecords); // Adiciona os novos registos
        }
    }
}