package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceivingEmailsWithAttachmentsService {
    private final ParsingAttachmentsPDFToEntityService parsingService;
    private final EmailRepository emailRepository;
    private final UpdateAttachmentRepository updateAttachmentRepository;

    @Scheduled(fixedRate = 10000)
    public void receiveAttachmentsAndSaveInDB() {
        try {
            for (Attachment attachment : GraphConfig.getAttachmentsList()) {
                if (!checkIfAttachmentIsNecessary(attachment)) {
                    continue;
                }
                if (checkIfHandledEmailsNotExistInBD()) {
                    saveNewEmailInDB(attachment);
                }

                List<UpdateAttachmentEntity> parsedFromPDFAttachments =
                        parsingService.parsingToUpdateAttachmentFromPDFAndSave(attachment.name, ((FileAttachment) attachment).contentBytes);
                EmailEntity email = emailRepository.findFirstByIsHandledIsFalse();
                parsedFromPDFAttachments.addAll(email.getUpdateAttachmentEntities());
                email.setUpdateAttachmentEntities(parsedFromPDFAttachments);
                emailRepository.save(email);
                log.info("Update email with id " + email.id + " in BD with new attachments.");

            }
        } catch (Exception e) {
            log.info("Can't received email or save it.");
        }
    }


    private boolean checkIfHandledEmailsNotExistInBD() {
        return emailRepository.findAllByIsHandledIsFalse().isEmpty();
    }

    private boolean checkIfAttachmentIsNecessary(Attachment attachment) throws SQLException {
        List<String> attachmentsNamesInDB = findAttachmentsNamesInDB();
        String attachmentName = attachment.name;

        if (attachmentName.contains(") - ")) {
            return Objects.equals(attachment.contentType, "application/pdf") &&
                    !attachmentsNamesInDB.contains(attachmentName.substring(0, attachmentName.indexOf(") - "))) &&
                    !attachmentsNamesInDB.contains(attachmentName);
        } else {
            return Objects.equals(attachment.contentType, "application/pdf") && !attachmentsNamesInDB.contains(attachmentName);
        }
    }

    private List<String> findAttachmentsNamesInDB() {
        return updateAttachmentRepository.findAllByOrderByIdAsc().stream().map(x -> x.name).collect(Collectors.toList());
    }

    private void saveNewEmailInDB(Attachment attachment) {
        EmailEntity email = EmailEntity.builder()
                .receivingTime(LocalDate.from(attachment.lastModifiedDateTime.toLocalDateTime()))
                .sendingTime(null)
                .build();

        emailRepository.save(email);
        log.info("Create new email with id " + email.id + " in BD.");
    }

}