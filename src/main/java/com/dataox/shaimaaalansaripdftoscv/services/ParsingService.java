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
public class ParsingService {
    private final UpdateAttachmentRepository updateAttachmentRepository;

    public void parsingToUpdateAttachmentFromPDFAndSave(String fileAttachmentName, byte[] filePDF) {
        PdfDocument attachmentInPDF = new PdfDocument(filePDF);
        PdfTableExtractor extractor = new PdfTableExtractor(attachmentInPDF);
        UpdateAttachmentEntity updateAttachment;

        for (PdfTable table : extractor.extractTable(0)) {
            updateAttachment = createUpdateAttachment(table);
            updateAttachment.setName(fileAttachmentName);
            saveUpdateAttachmentToDB(updateAttachment);
        }
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
        String day = table.getText(2, 45);
        String month = table.getText(2, 47);
        String date = ((day.length() < 2) ? "0" + day : day) +
                "-" +
                ((month.length() < 2) ? "0" + month : month) +
                "-" +
                table.getText(2, 51);
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
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
                    .FTG(table.getText(row, 62))
                    .hours(table.getText(row, 74))
                    .FPH(table.getText(row, 77))
                    .serNo(table.getText(row, 83))
                    .manufacturer(table.getText(row, 88))
                    .RPM(table.getText(row + 3, 0))
                    .WOB(table.getText(row + 3, 4))
                    .PSI(table.getText(row + 3, 35))
                    .liner(table.getText(row + 3, 44))
                    .SPM(table.getText(row + 3, 48))
                    .GPM(table.getText(row + 3, 58))
                    .PHHP(table.getText(row + 3, 64))
                    .BHHP(table.getText(row + 3, 74))
                    .TORQ(table.getText(row + 3, 77))
                    .NVEL(table.getText(row + 3, 86))
                    .AVEL(table.getText(row + 3, 92))
                    .I(table.getText(row + 3, 14))
                    .O(table.getText(row + 3, 17))
                    .D(table.getText(row + 3, 19))
                    .L(table.getText(row + 3, 21))
                    .B(table.getText(row + 3, 22))
                    .G(table.getText(row + 3, 26))
                    .Osecond(table.getText(row + 3, 31))
                    .R(table.getText(row + 3, 32).equals("BH") ?
                            table.getText(row + 3, 32) + "A" :
                            table.getText(row + 3, 32))
                    .build());
        }

        return bitHydraulicsEntities;
    }

}