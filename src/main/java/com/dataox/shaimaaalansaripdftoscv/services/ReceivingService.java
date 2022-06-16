package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceivingService {
    private final EmailRepository emailRepository;
    private final ParsingService parsingService;
    @Value("${spring.datasource.url}")
    private String DBURL;
    @Value("${spring.datasource.username}")
    private String userName;
    @Value("${spring.datasource.password}")
    private String password;

    //    @Scheduled(fixedDelay = 1000)
//    @EventListener(ApplicationReadyEvent.class)
    public void receiveAttachment() {
        try {
            GraphConfig.initializeGraph();
            for (Attachment attachment : GraphConfig.getAttachmentsList()) {
                if (checkIfAttachmentIsNecessary(attachment)) {
                    List<UpdateAttachmentEntity> updateAttachmentEntities =
                            parsingService.parsingToUpdateAttachmentFromPDF(attachment, ((FileAttachment) attachment).contentBytes);
                    if (checkIfHandledEmailsNotExistInBD()) {
                        EmailEntity emailEntity = EmailEntity.builder()
                                .receivingTime(LocalDate.from(attachment.lastModifiedDateTime.toLocalDateTime()))
                                .sendingTime(null)
                                .updateAttachmentEntities(updateAttachmentEntities)
                                .build();
                        emailRepository.save(emailEntity);
                        log.info("Create new email in BD.");
                    } else {
                        EmailEntity email = emailRepository.findFirstByIsHandledIsFalse();
                        updateAttachmentEntities.addAll(email.getUpdateAttachmentEntities());
                        email.setUpdateAttachmentEntities(updateAttachmentEntities);
                        emailRepository.save(email);
                        log.info("Update email in BD with new attachments.");
                    }
                }
            }
        } catch (Exception e) {
            log.info("Can't save received email.");
        }
    }

    private boolean checkIfAttachmentIsNecessary(Attachment attachment) throws SQLException {
        Connection dbConnection = DriverManager.getConnection(DBURL, userName, password);
        List<String> attachmentsNamesInDB = new ArrayList<>();

        try (Statement statement = dbConnection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select attachment_name from email order by id")) {
                while (resultSet.next()) {
                    attachmentsNamesInDB.add(resultSet.getString(1));
                }
            }
        }

        String attachmentName = attachment.name;
        if (attachmentName.contains(") - ")) {
            return Objects.equals(attachment.contentType, "application/pdf") &&
                    !attachmentsNamesInDB.contains(attachmentName.substring(0, attachmentName.indexOf(") - ")));
        } else {
            return Objects.equals(attachment.contentType, "application/pdf");
        }
    }

    private boolean checkIfHandledEmailsNotExistInBD() {
        return emailRepository.findAllByIsHandledIsFalse().isEmpty();
    }

}