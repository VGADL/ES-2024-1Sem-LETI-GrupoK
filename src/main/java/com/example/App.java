package com.example;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class App {
    public static void main(String[] args) {
       
        String filePath = "src/main/ficheiros/Madeira-Moodle.csv";
        String separador = ";";
        CsvReader csvReader = new CsvReader(filePath, separador);
        
        try {
            // Lê o ficheiro csv
            csvReader.readFile();
            // Obtém e mostra o cabeçalho
            System.out.println("Cabeçalho:");
            List<String> headers = csvReader.getHeaders();
            for (String header : headers) {
                System.out.print(header + "\t");
            }
            System.out.println();
            // Converte cada linha para um objeto CsvRecord e mostra os dados
            System.out.println("\nConteúdo do ficheiro:");
            List<CsvRecord> records = new ArrayList<>();
            // Use the records list
            System.out.println("\nTotal records: " + records.size());
            for (Map<String, String> recordData : csvReader.getRecords()) {
                CsvRecord record = new CsvRecord(recordData);
                records.add(record);
                for (String header : headers) {
                    System.out.println(header + ": " + record.getField(header));
                }
                System.out.println("----------------------------------"); // Separador
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro csv: " + e.getMessage());
        }
    }
}
