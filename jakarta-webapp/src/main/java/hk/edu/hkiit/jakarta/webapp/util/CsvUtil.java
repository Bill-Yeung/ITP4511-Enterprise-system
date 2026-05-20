package hk.edu.hkiit.jakarta.webapp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvUtil {

    public static ArrayList<String[]> readRows(BufferedReader reader) throws IOException {

        ArrayList<String[]> rows = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

        try (CSVParser parser = format.parse(reader)) {
            for (CSVRecord record : parser) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = clean(record.get(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace("\uFEFF", "").trim();
    }
    
}
