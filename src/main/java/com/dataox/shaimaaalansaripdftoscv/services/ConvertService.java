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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class ConvertService {
    private final EmailRepository emailRepository;
    private final SendingEmailsService sendingEmailsService;
    private final HandleErrorsService handleErrorsService;

    @Scheduled(cron = "${morning.scheduler}")
    @Scheduled(cron = "${day.scheduler}")
    public void createCSVFileAndSendWithEmail() {
        List<String> attachmentNames = new ArrayList<>();
        for (EmailEntity email : emailRepository.findAllByIsHandledIsFalse()) {
            try {
                UpdateAttachmentEntity updateAttachment = email.updateAttachment;
                List<String[]> csvData = convertEntityToCSV(updateAttachment);
                String attachmentName = "attachmentFiles/NPTReport_" + updateAttachment.name.substring(0, updateAttachment.name.length() - 4) + ".csv";
                try (CSVWriter writer = new CSVWriter(new FileWriter(attachmentName))) {
                    writer.writeAll(csvData);
                }
                attachmentNames.add(attachmentName);
            } catch (Exception e) {
                handleErrorsService.checkThatEmailHasErrorWhileSending(email);
            }
        }
        if (sendingEmailsService.isEmailCreatedAndSendToClient(attachmentNames)) {
            allNotHandledEmailsHasBeenSent();
            log.info("Email with attachments has been sent.");
        }
    }

    private void allNotHandledEmailsHasBeenSent() {
        for (EmailEntity email : emailRepository.findAllByIsHandledIsFalse()) {
            email.setHandled(true);
            email.setSendingTime(LocalDateTime.now());
            emailRepository.save(email);
        }
    }

    List<String[]> convertEntityToCSV(UpdateAttachmentEntity entity) {
        String[] blank = {" "};
        List<String[]> list = new ArrayList<>();

        list.add(new String[]{" ", " ", " ", "NP Report"});
        list.add(blank);
        list.add(new String[]{"Date: " + entity.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ", " + entity.date.getDayOfWeek()});
        list.add(blank);
        list.add(new String[]{"WELL NO", "TD TARGET", "PROFILE"});
        list.add(new String[]{entity.wellNo, entity.tgTarget, entity.profile});
        list.add(blank);
        list.add(new String[]{"AREA", "TEAM", "KOC TEAM LEADER", "RIG"});
        list.add(new String[]{entity.area, entity.team, entity.kocTeamLeader, entity.RIG});
        list.add(blank);
        list.add(new String[]{"Bit Hydraulics"});
        list.add(new String[]{"BIT NO.", "SIZE", "MODEL", "JET SIZE", "DEPTH IN", "DEPTH OUT", "FTG", "HOURS", "FPH",
                "SER NO.", "MANUFACTURER"});
        String[] bitColumnsValues;
        for (BITHydraulicsEntity bitHydraulics : entity.getBITHydraulics()) {
            bitColumnsValues = new String[]{bitHydraulics.BIT, bitHydraulics.size, bitHydraulics.model,
                    bitHydraulics.jetSize, bitHydraulics.depthIn, bitHydraulics.depthOut, bitHydraulics.FTG,
                    bitHydraulics.hours, bitHydraulics.FPH, bitHydraulics.serNo, bitHydraulics.manufacturer};
            list.add(bitColumnsValues);
        }
        list.add(blank);
        list.add(new String[]{"RPM", "WOB", "I", "O", "D", "L", "B", "G", "O", "R", "PSI", "LINER", "SPM", "GPM",
                "P.HHP", "B.HHP", "TORQ", "N.VEL", "A.VEL(DC/HW/DP)"});
        for (BITHydraulicsEntity bitHydraulics : entity.getBITHydraulics()) {
            bitColumnsValues = new String[]{bitHydraulics.RPM, bitHydraulics.WOB, bitHydraulics.I, bitHydraulics.O,
                    bitHydraulics.D, bitHydraulics.L, bitHydraulics.B, bitHydraulics.G, bitHydraulics.Osecond, bitHydraulics.R,
                    bitHydraulics.PSI, bitHydraulics.liner, bitHydraulics.SPM, bitHydraulics.GPM, bitHydraulics.PHHP,
                    bitHydraulics.BHHP, bitHydraulics.TORQ, bitHydraulics.NVEL, bitHydraulics.AVEL};
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
        list.add(new String[]{"HOURS", "DESCRIPTION"});
        String[] NPTColumnsValues;
        for (NonProductiveTimeEntity nonProductiveTime : entity.getNonProductiveTime()) {
            NPTColumnsValues = new String[]{String.valueOf(nonProductiveTime.hours), nonProductiveTime.operationalDistribution};
            list.add(NPTColumnsValues);
        }
        list.add(blank);

        return list;
    }

}
