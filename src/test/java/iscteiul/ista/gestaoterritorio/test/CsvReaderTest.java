package iscteiul.ista.gestaoterritorio.test;

import iscteiul.ista.gestaoterritorio.CsvReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {

    private CsvReader csvReader;
    private String testFilePath;
    private final String separator = ",";

    @BeforeEach
    void setUp() throws IOException {
        // Cria um arquivo CSV temporário para os testes
        testFilePath = "test.csv";
        String csvContent = """
                Name,Age,City
                Alice,30,New York
                Bob,25,Los Angeles
                Charlie,35,Chicago
                """;
        Files.writeString(Path.of(testFilePath), csvContent);

        csvReader = new CsvReader(testFilePath, separator);
    }

    @Test
    void testReadFile() throws IOException {
        csvReader.readFile();

        // Verifica os cabeçalhos
        List<String> headers = csvReader.getHeaders();
        assertEquals(3, headers.size(), "Deve haver 3 cabeçalhos.");
        assertEquals(List.of("Name", "Age", "City"), headers, "Os cabeçalhos devem corresponder.");

        // Verifica os registros
        List<Map<String, String>> records = csvReader.getRecords();
        assertEquals(3, records.size(), "Deve haver 3 registros.");

        // Verifica o conteúdo do primeiro registro
        Map<String, String> firstRecord = records.get(0);
        assertEquals("Alice", firstRecord.get("Name"));
        assertEquals("30", firstRecord.get("Age"));
        assertEquals("New York", firstRecord.get("City"));

        // Verifica o conteúdo do segundo registro
        Map<String, String> secondRecord = records.get(1);
        assertEquals("Bob", secondRecord.get("Name"));
        assertEquals("25", secondRecord.get("Age"));
        assertEquals("Los Angeles", secondRecord.get("City"));
    }

    @Test
    void testSetRecords() {
        // Prepara novos registros para configurar no CsvReader
        List<Map<String, String>> newRecords = new ArrayList<>();
        Map<String, String> newRecord = new HashMap<>();
        newRecord.put("Name", "Diana");
        newRecord.put("Age", "28");
        newRecord.put("City", "Miami");
        newRecords.add(newRecord);

        // Configura os novos registros
        csvReader.setRecords(newRecords);

        // Verifica que os registros foram atualizados
        List<Map<String, String>> updatedRecords = csvReader.getRecords();
        assertEquals(1, updatedRecords.size(), "Deve haver apenas 1 registro após a atualização.");
        assertEquals("Diana", updatedRecords.get(0).get("Name"));
        assertEquals("28", updatedRecords.get(0).get("Age"));
        assertEquals("Miami", updatedRecords.get(0).get("City"));
    }

    @Test
    void testEmptyCsvFile() throws IOException {
        // Cria um arquivo CSV vazio
        Files.writeString(Path.of(testFilePath), "");

        csvReader.readFile();

        // Verifica que os cabeçalhos e registros estão vazios
        assertTrue(csvReader.getHeaders().isEmpty(), "Os cabeçalhos devem estar vazios para um arquivo CSV vazio.");
        assertTrue(csvReader.getRecords().isEmpty(), "Os registros devem estar vazios para um arquivo CSV vazio.");
    }
}
