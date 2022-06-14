package com.dataox.shaimaaalansaripdftoscv.services;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SendingService {

    public static void givenDataArray_whenConvertToCSV_thenOutputCreated() throws IOException {
        List<String[]> csvData = createCsvDataSimple();

        try (CSVWriter writer = new CSVWriter(new FileWriter("/home/lusika/Downloads/test.csv"))) {
            writer.writeAll(csvData);
        }
    }

    private static List<String[]> createCsvDataSimple() {
        String[] header = {"id", "name", "address", "phone"};
        String[] record1 = {"1", "first name", "address 1", "11111"};
        String[] record2 = {"2", "second name", "address 2", "22222"};

        List<String[]> list = new ArrayList<>();
        list.add(header);
        list.add(record1);
        list.add(record2);

        return list;
    }

}
