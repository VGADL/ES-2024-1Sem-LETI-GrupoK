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

    public CsvReader(String filePath, String separator) {
        this.filePath = filePath;
        this.separator = separator;
    }

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

    public List<Map<String, String>> getRecords() {
        return records;
    }

    public List<String> getHeaders() {
        return headers;
    }

}
