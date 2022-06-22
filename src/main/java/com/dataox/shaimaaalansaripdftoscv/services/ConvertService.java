package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.BITHydraulicsEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.NonProductiveTimeEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Paths;
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
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 19, Font.BOLD);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font smallText = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static final Font smallTables = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

    @Scheduled(cron = "${morning.scheduler}")
    @Scheduled(cron = "${day.scheduler}")
    public void createPDFFileAndSendWithEmail() {
        java.util.List<String> attachmentNames = new ArrayList<>();
        List<EmailEntity> correctEmails = new ArrayList<>();
        List<EmailEntity> failedEmails = new ArrayList<>();
        for (EmailEntity email : emailRepository.findAllByHandledIsFalse()) {
            try {
                UpdateAttachmentEntity updateAttachment = email.updateAttachment;
                String attachmentName = "attachmentFiles/NPTReport_" + updateAttachment.name.substring(0, updateAttachment.name.length() - 4) + ".pdf";
                attachmentNames.add(attachmentName);

                Document document = new Document(PageSize.A4.rotate(), 18, 18, 10, 15);
                PdfWriter.getInstance(document, new FileOutputStream(String.valueOf((Paths.get(attachmentName)))));
                document.open();
                addMetaData(document);
                addPage(document, updateAttachment);
                document.close();
                correctEmails.add(email);
            } catch (Exception e) {
                failedEmails.add(email);
                log.info("Error in converting to PDF.");
            }
        }
        if (sendingEmailsService.isEmailCreatedAndSendToClient(attachmentNames)) {
            allNotHandledEmailsHasBeenSent(correctEmails);
            log.info("Email with attachments has been sent.");
        }
        handleErrorsService.checkThatEmailHasErrorWhileSending(failedEmails);
    }

    private void allNotHandledEmailsHasBeenSent(List<EmailEntity> correctEmails) {
        LocalDateTime now = LocalDateTime.now();
        for (EmailEntity email : correctEmails) {
            email.setHandled(true);
            email.setSendingTime(now);
            emailRepository.save(email);
        }
    }

    private void addPage(Document document, UpdateAttachmentEntity entity) throws DocumentException {
        Paragraph header = new Paragraph("NP Report", catFont);
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
        createTableFirst(paragraph, entity);
        addEmptyLine(paragraph, 1);
        createTableSecond(paragraph, entity);
        addEmptyLine(paragraph, 1);
        paragraph.add(new Paragraph("Bit Hydraulics", subFont));
        addEmptyLine(paragraph, 1);
        createTableThird(paragraph, entity);
        addEmptyLine(paragraph, 1);
        paragraph.add(new Paragraph("Drilling BHA", subFont));
        paragraph.add(new Paragraph(entity.drillingBHA, smallText));
        addEmptyLine(paragraph, 1);
        paragraph.add(new Paragraph("Present Activity", subFont));
        paragraph.add(new Paragraph(entity.presentActivity, smallText));
        addEmptyLine(paragraph, 1);
        paragraph.add(new Paragraph("Formation", subFont));
        paragraph.add(new Paragraph(entity.formation, smallText));
        addEmptyLine(paragraph, 1);
        paragraph.add(new Paragraph("Non-Productive Time (NPT)", subFont));
        addEmptyLine(paragraph, 1);
        createTableForth(paragraph, entity);
    }

    private void createTableFirst(Paragraph paragraph, UpdateAttachmentEntity entity) {
        LineDash solid = new SolidLine();
        PdfPTable table = new PdfPTable(3);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(90);
        List<String> headers = List.of("WELL NO", "TD TARGET", "PROFILE");
        List<String> values = List.of(entity.wellNo, entity.tgTarget, entity.profile);

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


    private void createTableSecond(Paragraph paragraph, UpdateAttachmentEntity entity) {
        LineDash solid = new SolidLine();
        PdfPTable table = new PdfPTable(4);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(90);
        List<String> headers = List.of("AREA", "TEAM", "KOC TEAM LEADER", "RIG");
        List<String> values = List.of(entity.area, entity.team, entity.kocTeamLeader, entity.RIG);

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

    private void createTableThird(Paragraph paragraph, UpdateAttachmentEntity entity) {
        LineDash solid = new SolidLine();
        PdfPTable table = new PdfPTable(new float[]{10, 10, 25, 35, 15, 15, 10, 10, 10, 15, 25});
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(100);
        List<String> headers = List.of("BIT NO.", "SIZE", "MODEL", "JET SIZE", "DEPTH IN", "DEPTH OUT", "FTG",
                "HOURS", "FPH", "SER NO.", "MANUFACTURER");
        List<String> values;

        for (String header : headers) {
            PdfPCell c1 = new PdfPCell(new Phrase(header, smallTables));
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setCellEvent(new CustomBorder(null, null, null, solid));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c1);
        }
        table.setHeaderRows(1);
        for (BITHydraulicsEntity bitHydraulics : entity.getBITHydraulics()) {
            values = List.of(bitHydraulics.BIT, bitHydraulics.size, bitHydraulics.model,
                    bitHydraulics.jetSize, bitHydraulics.depthIn, bitHydraulics.depthOut, bitHydraulics.FTG,
                    bitHydraulics.hours, bitHydraulics.FPH, bitHydraulics.serNo, bitHydraulics.manufacturer);
            for (String value : values) {
                PdfPCell c1 = new PdfPCell(new Phrase(value, smallTables));
                c1.setBorder(Rectangle.NO_BORDER);
                c1.setCellEvent(new CustomBorder(null, null, null, solid));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(c1);
            }
        }
        paragraph.add(table);
        addEmptyLine(paragraph, 1);

        table = new PdfPTable(new float[]{5, 7, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 3, 5, 5, 5, 8, 5, 9});
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(100);
        headers = List.of("RPM", "WOB", "I", "O", "D", "L", "B", "G", "O", "R", "PSI", "LINER", "SPM", "GPM",
                "P.HHP", "B.HHP", "TORQ", "N.VEL", "A.VEL(DC/HW/DP)");

        for (String header : headers) {
            PdfPCell c1 = new PdfPCell(new Phrase(header, smallTables));
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setCellEvent(new CustomBorder(null, null, null, solid));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c1);
        }
        table.setHeaderRows(1);
        for (BITHydraulicsEntity bitHydraulics : entity.getBITHydraulics()) {
            values = List.of(bitHydraulics.RPM, bitHydraulics.WOB, bitHydraulics.I, bitHydraulics.O,
                    bitHydraulics.D, bitHydraulics.L, bitHydraulics.B, bitHydraulics.G, bitHydraulics.Osecond, bitHydraulics.R,
                    bitHydraulics.PSI, bitHydraulics.liner, bitHydraulics.SPM, bitHydraulics.GPM, bitHydraulics.PHHP,
                    bitHydraulics.BHHP, bitHydraulics.TORQ, bitHydraulics.NVEL, bitHydraulics.AVEL);
            for (String value : values) {
                PdfPCell c1 = new PdfPCell(new Phrase(value, smallTables));
                c1.setBorder(Rectangle.NO_BORDER);
                c1.setCellEvent(new CustomBorder(null, null, null, solid));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(c1);
            }
        }

        paragraph.add(table);
    }

    private void createTableForth(Paragraph paragraph, UpdateAttachmentEntity entity) {
        LineDash solid = new SolidLine();
        PdfPTable table = new PdfPTable(new float[]{10, 90});
        table.setWidthPercentage(100);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        List<String> headers = List.of("HOURS", "DESCRIPTION");
        List<String> values;

        for (String header : headers) {
            PdfPCell c1 = new PdfPCell(new Phrase(header, smallTables));
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setCellEvent(new CustomBorder(null, null, null, solid));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c1);
        }
        for (NonProductiveTimeEntity nonProductiveTime : entity.nonProductiveTime) {
            values = List.of(String.valueOf(nonProductiveTime.hours), nonProductiveTime.operationalDistribution);
            for (String value : values) {
                PdfPCell c1 = new PdfPCell(new Phrase(value, smallTables));
                c1.setBorder(Rectangle.NO_BORDER);
                c1.setCellEvent(new CustomBorder(null, null, null, solid));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(c1);
            }
        }

        paragraph.add(table);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private static void addMetaData(Document document) {
        document.addTitle("NP Report");
        document.addAuthor("Dataox");
        document.addCreator("Dataox");
    }

    class CustomBorder implements PdfPCellEvent {
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

    class SolidLine implements LineDash {
        public void applyLineDash(PdfContentByte canvas) {
        }
    }

    interface LineDash {
        void applyLineDash(PdfContentByte canvas);
    }

}
