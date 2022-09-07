package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.entities.UpdateAttachmentEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.dataox.shaimaaalansaripdftoscv.repositories.UpdateAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceivingFilesService {
    private final UpdateAttachmentRepository updateAttachmentRepository;
    private final ParsingService parsingService;
    private final EmailRepository emailRepository;
    @Value("${docs.path}")
    private String folder;
    @Value("${docs.checkDate}")
    private String checkDate;

    public void receiveAttachmentsAndSaveInDB() {
        try {
            File[] files = new File(folder).listFiles();
            log.info("folder path: " + folder);
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));

            for (File file : files) {
                LocalDateTime fileDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), TimeZone.getDefault().toZoneId());
                if (!checkIfFileIsNecessary(file)) {
                    continue;
                }
                log.info("file name: " + file.getName());
                try {
                    EmailEntity newEmail = saveNewEmailInDBAndReturn(fileDate);
                    parseAndUpdateEmailInDBWithMewAttachment(newEmail, file);
                } catch (Exception e) {
                    log.info("Can't received file or save it: " + e);
                }
            }
        } catch (Exception e) {
            log.info("There are no useful documents in folder.");
            log.info(e);
        }
    }


    private boolean checkIfFileIsNecessary(File file) {
        String fileName = file.getName();
        if (!Objects.equals(FilenameUtils.getExtension(fileName), "PDF"))
            return false;
        List<String> attachmentsNamesInDB = findAttachmentsNamesInDB();
        String dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

        fileName = fileName.substring(0, fileName.indexOf(".PDF"));
        if (!fileName.contains("Extracted_") && fileName.contains(dateToday) && !attachmentsNamesInDB.contains(fileName)) {
            if (fileName.contains(")-") || fileName.contains(") -")) {
                String finalFileName = fileName;
                return attachmentsNamesInDB.stream().noneMatch(x -> x.contains((finalFileName.substring(0, finalFileName.indexOf(")") + 1))));
            } else
                return true;
        } else return false;
    }

    private List<String> findAttachmentsNamesInDB() {
        return updateAttachmentRepository.findAllByOrderByIdAsc().stream().map(x -> x.name.substring(0, x.name.indexOf(".PDF"))).collect(Collectors.toList());
    }

    private EmailEntity saveNewEmailInDBAndReturn(LocalDateTime emailReceivingTime) {
        EmailEntity email = EmailEntity.builder()
                .receivingTime(emailReceivingTime)
                .sendingTime(null)
                .build();
        emailRepository.save(email);
        return email;
    }

    private void parseAndUpdateEmailInDBWithMewAttachment(EmailEntity email, File file) throws IOException {
        UpdateAttachmentEntity updateAttachment = parsingService.parsingToUpdateAttachmentFromPDFAndSave(file.getName(), Files.readAllBytes(file.toPath()));
        if (updateAttachment.getNonProductiveTime() != null && !updateAttachment.getNonProductiveTime().isEmpty()) {
            email.setUpdateAttachment(updateAttachment);
            emailRepository.save(email);
        } else {
            email.setHandled(true);
            emailRepository.save(email);
        }
    }

}
