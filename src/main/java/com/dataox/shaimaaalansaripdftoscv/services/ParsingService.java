package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.BITHydraulicsEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.NonProductiveTimeEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;
import com.spire.pdf.widget.PdfPageCollection;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.substringAfter;

@Log4j2
@Service
@AllArgsConstructor
public class ParsingService {
    private final UpdateAttachmentRepository updateAttachmentRepository;

    public UpdateAttachmentEntity parsingToUpdateAttachmentFromPDFAndSave(String fileAttachmentName, byte[] filePDF) {
        PdfDocument attachmentInPDF = new PdfDocument(filePDF);
        PdfTableExtractor extractor = new PdfTableExtractor(attachmentInPDF);
        UpdateAttachmentEntity updateAttachment = null;

        for (PdfTable table : extractor.extractTable(0)) {
            PdfPageCollection pdfPageCollection = attachmentInPDF.getPages();
            updateAttachment = createUpdateAttachment(table, pdfPageCollection.get(0));
            if (updateAttachment.getNonProductiveTime() != null && !updateAttachment.getNonProductiveTime().isEmpty()) {
                updateAttachment.setName(fileAttachmentName);
                saveUpdateAttachmentToDB(updateAttachment);
            }
        }
        return updateAttachment;
    }


    private void saveUpdateAttachmentToDB(UpdateAttachmentEntity attachment) {
        updateAttachmentRepository.save(attachment);
        log.info("Attachment with id " + attachment.id + " saved in DB.");
    }

    private UpdateAttachmentEntity createUpdateAttachment(PdfTable table, PdfPageBase pdfPageBase) {
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
                .kocTeamLeader(pdfPageBase.extractText(new Rectangle(470, 730, 120, 10))
                        .substring(pdfPageBase.extractText(new Rectangle(470, 730, 120, 10)).indexOf("Java.") + 5).trim())
                .date(parsingDateFromPDF(table))
                .BITHydraulics(parsingAndSaveBITFromPDF(table, pdfPageBase))
                .nonProductiveTime(!parsingAndSaveNonProductiveTimeFromPDF(table).isEmpty() ? parsingAndSaveNonProductiveTimeFromPDF(table) : null)
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

    private List<BITHydraulicsEntity> parsingAndSaveBITFromPDF(PdfTable table, PdfPageBase pdfPageBase) {
        List<BITHydraulicsEntity> bitHydraulicsEntities = new ArrayList<>();
        for (int row = 7; row <= 8; row++) {
            BITHydraulicsEntity bitHydraulics = BITHydraulicsEntity.builder()
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
                    .build();
            String s;
            if (row == 7) {
                s = pdfPageBase.extractText(new Rectangle(185, 175, 13, 13));
            } else {
                s = pdfPageBase.extractText(new Rectangle(185, 185, 13, 13));
            }
            bitHydraulics.setR(substringAfter(s, "Java.").trim());
            bitHydraulicsEntities.add(bitHydraulics);
        }
        return bitHydraulicsEntities;
    }

}