package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;
import com.spire.pdf.widget.PdfPageCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ParsingService {
    private final UpdateAttachmentRepository updateAttachmentRepository;


    public UpdateAttachmentEntity parsingToUpdateAttachmentFromPDFAndSave(String fileAttachmentName, byte[] filePDF) {
        PdfDocument attachmentInPDF = new PdfDocument(filePDF);
        PdfTableExtractor extractor = new PdfTableExtractor(attachmentInPDF);
        UpdateAttachmentEntity updateAttachment = null;

        for (PdfTable table : extractor.extractTable(0)) {
            PdfPageCollection pageCollection = attachmentInPDF.getPages();
            updateAttachment = createUpdateAttachment(table, pageCollection.get(0));
            updateAttachment.setName(fileAttachmentName);
            saveUpdateAttachmentToDB(updateAttachment);
        }
        return updateAttachment;
    }


    private void saveUpdateAttachmentToDB(UpdateAttachmentEntity attachment) {
        updateAttachmentRepository.save(attachment);
    }

    private UpdateAttachmentEntity createUpdateAttachment(PdfTable table, PdfPageBase pdfPageBase) {
        String firstRowInPDF = table.getText(0, 0);
        return UpdateAttachmentEntity.builder()
                .header(firstRowInPDF.substring(firstRowInPDF.indexOf("Field") + 5, firstRowInPDF.indexOf("GC")))
                .header(table.getText(2, 0))
                .header(pdfPageBase.extractText(new Rectangle(470, 730, 120, 10))
                        .substring(pdfPageBase.extractText(new Rectangle(470, 730, 120, 10)).indexOf("Field") + 5).trim())
                .date(parsingDateFromPDF(table))
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

}