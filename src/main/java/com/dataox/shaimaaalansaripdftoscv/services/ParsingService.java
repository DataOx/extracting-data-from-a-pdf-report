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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Log4j2
@Service
@AllArgsConstructor
public class ParsingService {

    private final UpdateAttachmentRepository updateAttachmentRepository;

    public void parsingToUpdateAttachmentFromPDF(byte[] filePDF) throws ParseException {
        PdfDocument attachmentInPDF = new PdfDocument(filePDF);
        PdfTableExtractor extractor = new PdfTableExtractor(attachmentInPDF);
        for (PdfTable table : extractor.extractTable(0)) {
            saveUpdateAttachmentToDB(createUpdateAttachment(table));
        }
    }

    private void saveUpdateAttachmentToDB(UpdateAttachmentEntity attachment) {
        updateAttachmentRepository.save(attachment);
        log.info("Attachment with id " + attachment.id + " saved in DB.");
    }

    private UpdateAttachmentEntity createUpdateAttachment(PdfTable table) throws ParseException {
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

    private Date parsingDateFromPDF(PdfTable table) throws ParseException {
        StringBuilder stringBuilderForDateField = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        stringBuilderForDateField
                .append(table.getText(2, 45)).append("-")
                .append(table.getText(2, 47)).append("-")
                .append(table.getText(2, 51));

        return formatter.parse(stringBuilderForDateField.toString());
    }

    private NonProductiveTimeEntity parsingAndSaveNonProductiveTimeFromPDF(PdfTable table) {
        //        StringBuilder stringBuilderForDateField = new StringBuilder();
//        String text = table.getText(7, 21);
//        stringBuilderForDateField.append(text).append(" ");
//        text = table.getText(7, 44);
//        stringBuilderForDateField.append(text).append(" ");
//        stringBuilderForDateField.append("\r\n");
//        System.out.println("вона: " + stringBuilderForDateField);

        return NonProductiveTimeEntity.builder()
                .build();
    }

    private BITHydraulicsEntity parsingAndSaveBITFromPDF(PdfTable table) {

        return BITHydraulicsEntity.builder()
                .BIT(table.getText(7, 0))
                .size(table.getText(7, 2))
                .model(table.getText(7, 9))
                .jetSize(table.getText(7, 21))
                .depthIn(table.getText(7, 44))
                .depthOut(table.getText(7, 49))
                .serNo(table.getText(7, 83))
                .manufacturer(table.getText(7, 88))
                .PSI(table.getText(10, 35))
                .liner(table.getText(10, 44))
                .SPM(table.getText(10, 51))
                .PHHP(table.getText(10, 58))
                .AVEL(table.getText(10, 92))
                .I(table.getText(11, 14))
                .O(table.getText(11, 17))
                .D(table.getText(11, 19))
                .L(table.getText(11, 21))
                .B(table.getText(11, 22))
                .G(table.getText(11, 26))
                .Osecond(table.getText(11, 31))
                .R(table.getText(11, 32))
                .build();
    }

}