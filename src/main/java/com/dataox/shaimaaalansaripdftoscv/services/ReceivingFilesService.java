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

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceivingFilesService {
    private final ParsingService parsingService;
    private final UpdateAttachmentRepository updateAttachmentRepository;
    private final EmailRepository emailRepository;
    @Value("${docs.path}")
    private String folder;
    @Value("${docs.checkDate}")
    private String checkDate;


    public void receiveAttachments() {
        try {
            File[] files = new File(folder).listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));

            for (File file : files) {
                if (!checkIfFileIsNecessary(file)) {
                    continue;
                }
                log.info("Receive file with name: " + file.getName());
                saveAttachmentInDB(file);
            }
        } catch (Exception e) {
            log.info("There are no useful documents in folder.");
            log.info(e);
        }
    }

    private void saveAttachmentInDB(File file) {
        try {
            LocalDateTime emailReceivingTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), TimeZone.getDefault().toZoneId());
            EmailEntity newEmail = saveNewEmailInDBAndReturn(emailReceivingTime);
            parseAndUpdateEmailInDBWithMewAttachment(newEmail, file);
        } catch (Exception e) {
            log.info("Can't received file or save it: " + e);
        }
    }

    private boolean checkIfFileIsNecessary(File file) {
        String fileName = file.getName();
        if (!Objects.equals(FilenameUtils.getExtension(fileName), "PDF"))
            return false;

        List<String> attachmentsInDB = findAttachmentsNamesInDB();
        String dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        fileName = fileName.substring(0, fileName.indexOf(".PDF"));
        if (fileName.contains(dateToday) && !attachmentsInDB.contains(fileName)) {
            if (fileName.indexOf(")") + 1 != fileName.length()) {
                fileName = fileName.substring(0, fileName.indexOf(")") + 1);
            }
            String finalFileName1 = fileName;
            return attachmentsInDB.stream().filter(x -> x.substring(0, x.indexOf(")") + 1).contains(finalFileName1)).findAny().isEmpty();
        } else return false;
    }

    private List<String> findAttachmentsNamesInDB() {
        return updateAttachmentRepository.findAllByOrderByIdAsc().stream().map(x -> x.name.substring(0, x.name.indexOf(".PDF"))).toList();
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
        email.setUpdateAttachment(updateAttachment);
        emailRepository.save(email);
    }

}
