package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class SendingService {

    public static void sendEmail() throws Exception {
        GraphConfig.initializeGraph();
        try {
            final User senderUser = GraphConfig.getRecipientUser();
            final String recipientEmailAddress = senderUser.mail == null ? senderUser.userPrincipalName : senderUser.mail;
            AttachmentCollectionResponse attachment = new AttachmentCollectionResponse();
            attachment.value = List.of(getFileAttachment());
            GraphConfig.sendMail("Update Report", createEmail(), attachment, recipientEmailAddress);
            log.info("Mail with CSV sent.");
        } catch (Exception e) {
            log.info("Error sending mail");
            log.info(e.getMessage());
        }

    }

    private static FileAttachment getFileAttachment() throws Exception {
        File pdfFile = new File("NPTReport.csv");
        InputStream fileStream = Files.newInputStream(pdfFile.toPath());
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.name = pdfFile.getName();
        fileAttachment.contentBytes = getByteArray(fileStream);
        fileAttachment.oDataType = "#microsoft.graph.fileAttachment";
        fileAttachment.size = Math.toIntExact((pdfFile.length() / 1024) / 1024);
        fileAttachment.id = "521";
        return fileAttachment;
    }

    private static byte[] getByteArray(InputStream in) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String createEmail() {
        return "";
    }


}
