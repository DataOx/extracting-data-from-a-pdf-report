package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.domain.ConvertData;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class ConvertService {
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 19, Font.BOLD);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font smallTables = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    private final EmailRepository emailRepository;


    public ConvertData createPdfFiles(List<EmailEntity> emails) {
        Map<String, byte[]> attachments = new HashMap<>();
        List<EmailEntity> correctEmails = new ArrayList<>();
        List<EmailEntity> failedEmails = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (EmailEntity email : emails) {
            if (!email.handled) {
                try {
                    UpdateAttachmentEntity updateAttachment = email.updateAttachment;
                    String attachmentName = updateAttachment.name.substring(0, updateAttachment.name.length() - 4) + ".pdf";

                    ByteArrayOutputStream docOutput = new ByteArrayOutputStream();
                    Document document = new Document(PageSize.A4.rotate(), 18, 18, 10, 15);
                    PdfWriter.getInstance(document, docOutput);
                    document.open();
                    addMetaData(document);
                    addPage(document, updateAttachment);
                    document.close();

                    attachments.put(attachmentName, docOutput.toByteArray());
                    email.setHandled(true);
                    email.setHasSendingError(false);
                    email.setSendingTime(now);
                    emailRepository.save(email);
                    docOutput.close();
                } catch (Exception e) {
                    email.setHasSendingError(true);
                    email.setHandled(true);
                    emailRepository.save(email);
                    log.info("Error in converting to PDF: " + e);
                }
            }
        }

        return new ConvertData(attachments, correctEmails, failedEmails);
    }

    private void addPage(Document document, UpdateAttachmentEntity entity) throws DocumentException {
        Paragraph header = new Paragraph("Header", catFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        addEmptyLine(header, 1);
        Paragraph content = new Paragraph();
        addEmptyLine(content, 2);
        content.add(new Paragraph("Date:  " + entity.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + ", " + entity.date.getDayOfWeek(), subFont));
        addTables(content, entity);
        addEmptyLine(content, 1);
        document.add(content);
    }

    private void addTables(Paragraph paragraph, UpdateAttachmentEntity entity) {
        paragraph.setAlignment(Element.ALIGN_CENTER);
        addEmptyLine(paragraph, 1);
        paragraph.add(new Paragraph("Paragraph", subFont));
        createTableFirst(paragraph, entity);
        addEmptyLine(paragraph, 1);
    }

    private void createTableFirst(Paragraph paragraph, UpdateAttachmentEntity entity) {
        LineDash solid = new SolidLine();
        PdfPTable table = new PdfPTable(3);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(90);
        List<String> headers = List.of("Header", "Header", "Header");
        List<String> values = List.of(entity.header, entity.header, entity.header);

        for (String header : headers) {
            PdfPCell c1 = new PdfPCell(new Phrase(header, smallTables));
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setCellEvent(new CustomBorder(null, null, null, solid));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c1);
        }
        table.setHeaderRows(1);
        for (String value : values) {
            PdfPCell c1 = new PdfPCell(new Phrase(value, smallTables));
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setCellEvent(new CustomBorder(null, null, null, solid));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c1);
        }

        paragraph.add(table);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private static void addMetaData(Document document) {
        document.addTitle("Title");
        document.addAuthor("Data-Ox");
        document.addCreator("Data-Ox");
    }

    static class CustomBorder implements PdfPCellEvent {
        protected LineDash left;
        protected LineDash right;
        protected LineDash top;
        protected LineDash bottom;

        public CustomBorder(LineDash left, LineDash right,
                            LineDash top, LineDash bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
            if (top != null) {
                canvas.saveState();
                top.applyLineDash(canvas);
                canvas.moveTo(position.getRight(), position.getTop());
                canvas.lineTo(position.getLeft(), position.getTop());
                canvas.stroke();
                canvas.restoreState();
            }
            if (bottom != null) {
                canvas.saveState();
                bottom.applyLineDash(canvas);
                canvas.moveTo(position.getRight(), position.getBottom());
                canvas.lineTo(position.getLeft(), position.getBottom());
                canvas.stroke();
                canvas.restoreState();
            }
            if (right != null) {
                canvas.saveState();
                right.applyLineDash(canvas);
                canvas.moveTo(position.getRight(), position.getTop());
                canvas.lineTo(position.getRight(), position.getBottom());
                canvas.stroke();
                canvas.restoreState();
            }
            if (left != null) {
                canvas.saveState();
                left.applyLineDash(canvas);
                canvas.moveTo(position.getLeft(), position.getTop());
                canvas.lineTo(position.getLeft(), position.getBottom());
                canvas.stroke();
                canvas.restoreState();
            }
        }
    }

    static class SolidLine implements LineDash {
        public void applyLineDash(PdfContentByte canvas) {
        }
    }

    interface LineDash {
        void applyLineDash(PdfContentByte canvas);
    }

}
