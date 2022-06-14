package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ReceivingService {
    private final EmailRepository emailRepository;
    private final ParsingService parsingService;
    @Value("${spring.datasource.url}")
    private String DBURL;
    @Value("${spring.datasource.username}")
    private String userName;
    @Value("${spring.datasource.password}")
    private String password;

    @EventListener(ApplicationReadyEvent.class)
    public void receiveAttachment() {
        try {
            GraphConfig.initializeGraph();

            for (Attachment attachment : GraphConfig.listInbox()) {
                if (checkIfAttachmentIsNecessary(attachment)) {

                    EmailEntity emailEntity = EmailEntity.builder()
                            .attachmentName(attachment.name)
                            .receivingTime(LocalDate.from(attachment.lastModifiedDateTime.toLocalDateTime()))
                            .sendingTime(null)
                            .build();

                    emailRepository.save(emailEntity);
                    parsingService.parsingToUpdateAttachmentFromPDF(((FileAttachment) attachment).contentBytes);
                }
            }
        } catch (Exception e) {
            log.info("Щось не так");
        }
    }

    private boolean checkIfAttachmentIsNecessary(Attachment attachment) throws SQLException {

        Connection dbConnection = DriverManager.getConnection(DBURL, userName, password);
        List<String> names = new ArrayList<>();
        try (Statement s = dbConnection.createStatement()) {
            try (ResultSet rs = s.executeQuery("select attachment_name from email order by id")) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }

        return Objects.equals(attachment.contentType, "application/pdf") && !names.contains(attachment.name);
    }

}