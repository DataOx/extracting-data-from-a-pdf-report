package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.SubscribeConfig;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class ReceivingService {
    private final EmailRepository emailRepository;
    private final ParsingService parsingService;

    @EventListener(ApplicationReadyEvent.class)
    public void receiveAttachment() {
        try {
            SubscribeConfig.initializeGraph();

            for (Attachment attachment : SubscribeConfig.listInbox()) {
                if(checkIfAttachmentIsNecessary(attachment)) {
                    System.out.println("Attachments type: " + attachment.contentType);
                    System.out.println("Attachments name:" + attachment.name);

                    assert attachment.lastModifiedDateTime != null;
                    EmailEntity emailEntity = EmailEntity.builder()
                            .attachmentName(attachment.name)
                            .receivingTime(java.sql.Timestamp.valueOf(attachment.lastModifiedDateTime.toLocalDateTime()))
                            .hasSendingError(false)
                            .isHandled(false)
                            .sendingTime(null)
                            .build();

                    byte[] bytes = ((FileAttachment) attachment).contentBytes;

                    emailRepository.save(emailEntity);
                    parsingService.parsingToUpdateAttachmentFromPDF(bytes);
                }
            }
        } catch (Exception e) {
            System.out.println("a");
        }
    }

    private boolean checkIfAttachmentIsNecessary(Attachment attachment) {
        return Objects.equals(attachment.contentType, "application/pdf");
    }

}