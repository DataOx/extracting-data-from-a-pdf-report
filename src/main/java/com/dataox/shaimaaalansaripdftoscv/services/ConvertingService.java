package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.BITHydraulicsEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ConvertingService {

    private final UpdateAttachmentRepository updateAttachmentRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void createCSVFile() throws Exception {
        List<String[]> csvData = convertEntityToCSV();
        try (CSVWriter writer = new CSVWriter(new FileWriter("./NPTReport.csv"))) {
            writer.writeAll(csvData);
        }
        SendingService.sendEmail();
    }

    List<String[]> convertEntityToCSV() {
        UpdateAttachmentEntity entity = updateAttachmentRepository.findById(53L).get();

        String[] header = {" ", " ", " ", "NP Report"};
        String[] columnsNames = {"Well No", "TD Target", "Profile", " "};
        String[] columnValues = {entity.wellNo, entity.tgTarget, entity.profile, " "};
        String[] blank = {" "};
        String[] bitColumnsNames = {"BIT No", "Size", "Model", "Jet size", "Depth in", "Depth out", "FTG", "Hours", "FPH", "Ser No", "Manufacturer"};
        List<BITHydraulicsEntity> bitHydraulicsList = entity.getBITHydraulics();
        BITHydraulicsEntity bitHydraulics = bitHydraulicsList.get(0);
        String[] bitColumnsValues = {bitHydraulics.BIT, bitHydraulics.size, bitHydraulics.model, bitHydraulics.jetSize, bitHydraulics.depthIn,
        bitHydraulics.depthOut, bitHydraulics.FTG, bitHydraulics.hours, bitHydraulics.FPH, bitHydraulics.serNo, bitHydraulics.manufacturer};

        List<String[]> list = new ArrayList<>();
        list.add(header);
        list.add(blank);
        list.add(columnsNames);
        list.add(columnValues);
        list.add(blank);
        list.add(bitColumnsNames);
        list.add(bitColumnsValues);
        list.add(blank);

        return list;
    }

}
