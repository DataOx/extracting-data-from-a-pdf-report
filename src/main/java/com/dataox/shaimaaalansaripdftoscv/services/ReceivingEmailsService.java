package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceivingEmailsService {
    private final UpdateAttachmentRepository updateAttachmentRepository;
    private final ParsingService parsingService;
    private final EmailRepository emailRepository;

    public void receiveAttachmentsAndSaveInDB() {
        try {
            for (Attachment attachment : GraphConfig.getListOfEmailsAttachmentsThatReceiveLaterThenLastSaved(dateOfLastSavedEmail())) {
                if (!checkIfAttachmentIsNecessary(attachment)) {
                    continue;
                }
                EmailEntity newEmail = saveNewEmailInDBAndReturn(attachment.lastModifiedDateTime);
                updateEmailInDBWithMewAttachment(newEmail, attachment);
                log.info("Update email with id " + newEmail.id + " in BD with new attachments.");
            }
        } catch (Exception e) {
            log.info("Can't received email or save it: " + e);
        }
    }


    private LocalDateTime dateOfLastSavedEmail() {
        try {
            return (emailRepository.findTopByOrderByReceivingTimeDesc().receivingTime);
        } catch (Exception e) {
            log.info("There are no emails in DB.");
            return LocalDateTime.now().minusDays(3L);
        }
    }

    private boolean checkIfAttachmentIsNecessary(Attachment attachment) {
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

    private EmailEntity saveNewEmailInDBAndReturn(java.time.OffsetDateTime emailReceivingTime) {
        EmailEntity email = EmailEntity.builder()
                .receivingTime(emailReceivingTime.toLocalDateTime())
                .sendingTime(null)
                .build();

        emailRepository.save(email);
        log.info("Create new email with id " + email.id + " in BD.");
        return email;
    }

    private void updateEmailInDBWithMewAttachment(EmailEntity email, Attachment attachment) {
        parsingService.parsingToUpdateAttachmentFromPDFAndSave(attachment.name, ((FileAttachment) attachment).contentBytes);
        email.setUpdateAttachment(updateAttachmentRepository.findTopByOrderByIdDesc());
        emailRepository.save(email);
    }

}