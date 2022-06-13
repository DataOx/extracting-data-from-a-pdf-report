package com.dataox.shaimaaalansaripdftoscv.services;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ParsingService {

    public void parsingToUpdateAttachmentFromPDF(byte[] filePDF) {
        try {
            PdfReader pdfReader = new PdfReader(filePDF);

            int pages = pdfReader.getNumberOfPages();

            for (int i = 1; i <= pages; i++) {
                String pageContent = PdfTextExtractor.getTextFromPage(pdfReader, i);
                System.out.println("Content on Page " + i + ": " + pageContent);
            }

            pdfReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}