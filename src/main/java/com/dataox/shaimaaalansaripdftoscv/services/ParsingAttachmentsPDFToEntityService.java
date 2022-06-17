package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.BITHydraulicsEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.NonProductiveTimeEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class ParsingAttachmentsPDFToEntityService {
    private final UpdateAttachmentRepository updateAttachmentRepository;

    public List<UpdateAttachmentEntity> parsingToUpdateAttachmentFromPDFAndSave(String fileAttachmentName, byte[] filePDF) {
        UpdateAttachmentEntity updateAttachment;
        List<UpdateAttachmentEntity> updateAttachmentEntities = new ArrayList<>();
        PdfDocument attachmentInPDF = new PdfDocument(filePDF);
        PdfTableExtractor extractor = new PdfTableExtractor(attachmentInPDF);

        for (PdfTable table : extractor.extractTable(0)) {
            updateAttachment = createUpdateAttachment(table);
            updateAttachment.setName(fileAttachmentName);
            updateAttachmentEntities.add(updateAttachment);
            saveUpdateAttachmentToDB(updateAttachment);
        }
        return updateAttachmentEntities;
    }


    private void saveUpdateAttachmentToDB(UpdateAttachmentEntity attachment) {
        updateAttachmentRepository.save(attachment);
        log.info("Attachment with id " + attachment.id + " saved in DB.");
    }

    private UpdateAttachmentEntity createUpdateAttachment(PdfTable table) {
        String firstRowInPDF = table.getText(0, 0);
        return UpdateAttachmentEntity.builder()
                .area(firstRowInPDF.substring(firstRowInPDF.indexOf("AREA:") + 5, firstRowInPDF.indexOf("GC")))
                .team(firstRowInPDF.substring(firstRowInPDF.indexOf("TEAM:") + 5))
                .wellNo(table.getText(2, 0))
                .tgTarget(table.getText(2, 8))
                .RIG(table.getText(2, 28))
                .presentActivity(table.getText(2, 75))
                .drillingBHA(table.getText(14, 12))
                .profile(table.getText(15, 3))
                .formation(table.getText(56, 11))
                .kocTeamLeader(table.getText(57, 91))
                .date(parsingDateFromPDF(table))
                .BITHydraulics(parsingAndSaveBITFromPDF(table))
                .nonProductiveTime(parsingAndSaveNonProductiveTimeFromPDF(table))
                .build();
    }

    private LocalDate parsingDateFromPDF(PdfTable table) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-M-yyyy");
        String date = table.getText(2, 45) + "-" +
                table.getText(2, 47) + "-" +
                table.getText(2, 51);
        return LocalDate.parse(date, formatter);
    }

    private List<NonProductiveTimeEntity> parsingAndSaveNonProductiveTimeFromPDF(PdfTable table) {
        List<NonProductiveTimeEntity> nonProductiveTimeEntities = new ArrayList<>();

        for (int row = 30; row < 54; row++) {
            if (table.getText(row, 1).contains("NP")) {
                StringBuilder operationalDistribution = new StringBuilder(table.getText(row, 6));
                for (int rowNP = row + 1; rowNP < 54; rowNP++) {
                    if (!table.getText(rowNP, 0).isEmpty()) {
                        break;
                    }
                    operationalDistribution.append(" ").append(table.getText(rowNP, 6));
                }
                if (row == 53 && table.getText(30, 60).equals("")) {
                    for (int rowNP = 30; rowNP < 54; rowNP++) {
                        if (!table.getText(rowNP, 60).equals("")) {
                            break;
                        }
                        operationalDistribution.append(" ").append(table.getText(rowNP, 70));
                    }
                }
                nonProductiveTimeEntities.add(NonProductiveTimeEntity.builder()
                        .hours(BigDecimal.valueOf(Double.parseDouble(table.getText(row, 0))))
                        .operationalDistribution(operationalDistribution.toString())
                        .build());
            }

            if (table.getText(row, 63).contains("NP")) {
                StringBuilder operationalDistribution = new StringBuilder(table.getText(row, 70));
                for (int rowNP = row + 1; rowNP < 54; rowNP++) {
                    if (!table.getText(rowNP, 60).equals("")) {
                        break;
                    }
                    operationalDistribution.append(" ").append(table.getText(rowNP, 70));
                }
                nonProductiveTimeEntities.add(NonProductiveTimeEntity.builder()
                        .hours(BigDecimal.valueOf(Double.parseDouble(table.getText(row, 60))))
                        .operationalDistribution(operationalDistribution.toString())
                        .build());
            }
        }
        return nonProductiveTimeEntities;
    }

    private List<BITHydraulicsEntity> parsingAndSaveBITFromPDF(PdfTable table) {
        List<BITHydraulicsEntity> bitHydraulicsEntities = new ArrayList<>();
        for (int row = 7; row <= 8; row++) {
            bitHydraulicsEntities.add(BITHydraulicsEntity.builder()
                    .BIT(table.getText(row, 0))
                    .size(table.getText(row, 2))
                    .model(table.getText(row, 9))
                    .jetSize(table.getText(row, 21))
                    .depthIn(table.getText(row, 44))
                    .depthOut(table.getText(row, 49))
                    .serNo(table.getText(row, 83))
                    .manufacturer(table.getText(row, 88))
                    .PSI(table.getText(row + 3, 35))
                    .liner(table.getText(row + 3, 44))
                    .SPM(table.getText(row + 3, 48))
                    .GPM(table.getText(row + 3, 58))
                    .PHHP(table.getText(row + 3, 64))
                    .AVEL(table.getText(row + 3, 92))
                    .I(table.getText(row + 3, 14))
                    .O(table.getText(row + 3, 17))
                    .D(table.getText(row + 3, 19))
                    .L(table.getText(row + 3, 21))
                    .B(table.getText(row + 3, 22))
                    .G(table.getText(row + 3, 26))
                    .Osecond(table.getText(row + 3, 31))
                    .R(table.getText(row + 3, 32))
                    .build());

//            StringBuilder stringBuilderForDateField = new StringBuilder();
//            String text = table.getText(row + 3, 48);
//            stringBuilderForDateField.append(text).append(".");
//            text = table.getText(row + 3, 58);
//            stringBuilderForDateField.append(text).append(".");
//            text = table.getText(row + 3, 64);
//            stringBuilderForDateField.append(text).append("\r\n");
//            stringBuilderForDateField.append("\r\n");
//            System.out.println("строка: " + stringBuilderForDateField);
        }
        return bitHydraulicsEntities;
    }

}