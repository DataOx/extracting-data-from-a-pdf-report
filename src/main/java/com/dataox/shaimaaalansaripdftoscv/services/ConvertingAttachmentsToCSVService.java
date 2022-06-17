package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.BITHydraulicsEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.NonProductiveTimeEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class ConvertingAttachmentsToCSVService {
    private final EmailRepository emailRepository;
    private final SendingEmailToClientService sendingEmailToClientService;
    private final SendingErrorsHandlerService sendingErrorsHandlerService;

//    @Scheduled(cron = "0 30 10 * * ?")
@Scheduled(fixedDelay = 1000)
    public void createCSVFileAndSendWithEmail() {
        for (EmailEntity email : emailRepository.findAllByIsHandledIsFalse()) {
            try {
                for (UpdateAttachmentEntity updateAttachment : email.updateAttachmentEntities) {
                    List<String[]> csvData = convertEntityToCSV(updateAttachment);
                    try (CSVWriter writer = new CSVWriter(new FileWriter("attachmentFiles/NPTReport_" + updateAttachment.name + ".csv"))) {
                        writer.writeAll(csvData);
                    }
                    sendingEmailToClientService.createAndSendEmailToClient(updateAttachment.name);

                    email.setHandled(true);
                    email.setSendingTime(LocalDate.now());
                    emailRepository.save(email);
                    log.info("Email with " + updateAttachment.name + " has been sent.");
                }
            }
            catch (Exception e) {
                sendingErrorsHandlerService.checkThatEmailHasErrorWhileSending(email);
            }
        }
    }

    List<String[]> convertEntityToCSV(UpdateAttachmentEntity entity) {
        String[] bitColumnsValues;
        String[] NPTColumnsValues;
        String[] blank = {" "};
        String[] header = {" ", " ", " ", "NP Report"};
        String[] columnsNames = {"WELL NO", "TD TARGET", "PROFILE", " "};
        String[] columnValues = {entity.wellNo, entity.tgTarget, entity.profile, " "};
        String[] bitColumnsNames = {"BIT NO", "SIZE", "MODEL", "JET SIZE", "DEPTH IN", "DEPTH OUT", "FTG", "HOURS",
                "FPH", "SER NO", "MANUFACTURER"};
        String[] bitSecondColumnsNames = {"RPM", "WOB", "Model", "I", "O", "D", "L", "B", "G", "O", "R", "PSI", "LINER",
                "SPM", "GPM", "P.HHP", "B.HHP", "TORQ", "N.VEL", "A.VEL(DC/HW/DP)"};
        String[] NPTColumnsNames = {"HOURS", "DESCRIPTION"};

        List<String[]> list = new ArrayList<>();
        list.add(header);
        list.add(blank);
        list.add(columnsNames);
        list.add(columnValues);
        list.add(blank);
        list.add(new String[]{"Bit Hydraulics"});
        list.add(bitColumnsNames);
        for (BITHydraulicsEntity bitHydraulics : entity.getBITHydraulics()) {
            bitColumnsValues = new String[]{bitHydraulics.BIT, bitHydraulics.size, bitHydraulics.model,
                    bitHydraulics.jetSize, bitHydraulics.depthIn, bitHydraulics.depthOut, bitHydraulics.FTG,
                    bitHydraulics.hours, bitHydraulics.FPH, bitHydraulics.serNo, bitHydraulics.manufacturer};
            list.add(bitColumnsValues);
        }
        list.add(blank);
        list.add(bitSecondColumnsNames);
        for (BITHydraulicsEntity bitHydraulics : entity.getBITHydraulics()) {
            bitColumnsValues = new String[]{bitHydraulics.PRM, bitHydraulics.WOB, bitHydraulics.I, bitHydraulics.O,
                    bitHydraulics.D, bitHydraulics.L, bitHydraulics.B, bitHydraulics.G, bitHydraulics.O, bitHydraulics.R,
                    bitHydraulics.PSI, bitHydraulics.liner, bitHydraulics.SPM, bitHydraulics.GPM, bitHydraulics.PHHP,
                    bitHydraulics.BHHP, bitHydraulics.TORQ, bitHydraulics.AVEL};
            list.add(bitColumnsValues);
        }
        list.add(blank);
        list.add(new String[]{"Drilling BHA"});
        list.add(new String[]{entity.drillingBHA});
        list.add(blank);
        list.add(new String[]{"Present Activity"});
        list.add(new String[]{entity.presentActivity});
        list.add(blank);
        list.add(new String[]{"Formation"});
        list.add(new String[]{entity.formation});
        list.add(blank);
        list.add(new String[]{"Non-Productive Time (NPT)"});
        list.add(NPTColumnsNames);
        for (NonProductiveTimeEntity nonProductiveTime : entity.getNonProductiveTime()) {
            NPTColumnsValues = new String[]{String.valueOf(nonProductiveTime.hours), nonProductiveTime.operationalDistribution};
            list.add(NPTColumnsValues);
        }
        list.add(blank);

        return list;
    }

}
