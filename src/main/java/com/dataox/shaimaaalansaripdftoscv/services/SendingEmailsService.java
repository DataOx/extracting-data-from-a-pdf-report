package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class SendingEmailsService {
    @Value("${recipient.email}")
    private String recipientEmail;

    public boolean isEmailCreatedAndSendToClient(List<String> attachmentNames) {
        try {
            sendEmailToClient(attachmentNames);
            return true;
        } catch (Exception e) {
            log.info("Error sending mail: ");
            log.info(e.getMessage());
            return false;
        }
    }

    public void sendEmailToClient(List<String> attachmentNames) throws Exception {
        List<Attachment> fileAttachments = new ArrayList<>();
        AttachmentCollectionResponse attachment = new AttachmentCollectionResponse();
        for (String updateAttachmentName : attachmentNames) {
            fileAttachments.add(getFileAttachment(updateAttachmentName));
        }
        attachment.value = fileAttachments;
        GraphConfig.sendEmail("NP Report", createEmailsBody(), attachment, recipientEmail);
    }

    private static FileAttachment getFileAttachment(String updateAttachmentName) throws Exception {
        File pdfFile = new File(updateAttachmentName);
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

    private static String createEmailsBody() {
        return "";
    }


}
