package iscteiul.ista.gestaoterritorio;
import java.util.Map;

public class CsvRecord {
    
    final private Map<String, String> fields;

    public CsvRecord(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getField(String headerName) {
        return fields.getOrDefault(headerName, "");
    }

    @Override
    public String toString() {
        return fields.toString();
    }
}
