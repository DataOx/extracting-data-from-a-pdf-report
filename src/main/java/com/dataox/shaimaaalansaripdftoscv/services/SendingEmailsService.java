package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class SendingEmailsService {
    @Value("${recipient.email}")
    private String recipientEmail;

    public boolean isEmailCreatedAndSendToClient(Map<String, byte[]> attachments) {
        try {
            sendEmailToClient(attachments);
            return true;
        } catch (Exception e) {
            log.info("Error sending mail: ");
            log.info(e.getMessage());
            return false;
        }
    }

    public void sendEmailToClient(Map<String, byte[]> attachments) throws Exception {
        List<Attachment> fileAttachments = new ArrayList<>();
        AttachmentCollectionResponse attachment = new AttachmentCollectionResponse();
        for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
            fileAttachments.add(getFileAttachment(entry));
        }
        attachment.value = fileAttachments;
        GraphConfig.sendEmail("NP Report", createEmailsBody(), attachment, recipientEmail);
    }

    private static FileAttachment getFileAttachment(Map.Entry<String, byte[]> entry) throws Exception {
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.name = entry.getKey();
        fileAttachment.contentBytes = entry.getValue();
        fileAttachment.oDataType = "#microsoft.graph.fileAttachment";
        fileAttachment.size = Math.toIntExact((entry.getValue().length / 1024) / 1024);
        fileAttachment.id = "521";
        return fileAttachment;
    }

    private static String createEmailsBody() {
        return "";
    }


}
