package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
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
import java.time.LocalDateTime;
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

    public void receiveAttachmentsAndSaveInDB() {
        try {
            File[] files = new File(folder).listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));

            for (File file : files) {
                LocalDateTime fileDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), TimeZone.getDefault().toZoneId());

                if (fileDate.isAfter(dateOfLastSavedEmail())) {
                    if (!checkIfFileIsNecessary(file)) {
                        continue;
                    }
                    try {
                        EmailEntity newEmail = saveNewEmailInDBAndReturn(fileDate);
                        updateEmailInDBWithMewAttachment(newEmail, file);
                        log.info("Update email with id " + newEmail.id + " in BD with new attachments.");
                    } catch (Exception e) {
                        log.info("Can't received file or save it: " + e);
                    }
                }
            }
        } catch (Exception e) {
            log.info("There are no useful documents in folder.");
        }
    }


    private LocalDateTime dateOfLastSavedEmail() {
        try {
            return (emailRepository.findTopByOrderByReceivingTimeDesc().receivingTime);
        } catch (Exception e) {
            log.info("There are no files in DB, then we take files from 5 last days.");
            return LocalDateTime.now().minusDays(35L);
        }
    }

    private boolean checkIfFileIsNecessary(File file) {
        List<String> attachmentsNamesInDB = findAttachmentsNamesInDB();
        String fileName = file.getName();

        if (fileName.contains(") - ")) {
            return Objects.equals(FilenameUtils.getExtension(fileName), "PDF") &&
                    !attachmentsNamesInDB.contains(fileName.substring(0, fileName.indexOf(") - "))) &&
                    !attachmentsNamesInDB.contains(fileName) &&
                    !fileName.contains("Extracted_");
        } else {
            return Objects.equals(FilenameUtils.getExtension(fileName), "PDF") &&
                    !attachmentsNamesInDB.contains(fileName) &&
                    !fileName.contains("Extracted_");
        }
    }

    private List<String> findAttachmentsNamesInDB() {
        return updateAttachmentRepository.findAllByOrderByIdAsc().stream().map(x -> x.name).collect(Collectors.toList());
    }

    private EmailEntity saveNewEmailInDBAndReturn(LocalDateTime emailReceivingTime) {
        EmailEntity email = EmailEntity.builder()
                .receivingTime(emailReceivingTime)
                .sendingTime(null)
                .build();

        emailRepository.save(email);
        log.info("Create new email with id " + email.id + " in BD.");
        return email;
    }

    private void updateEmailInDBWithMewAttachment(EmailEntity email, File file) throws IOException {
        parsingService.parsingToUpdateAttachmentFromPDFAndSave(file.getName(), Files.readAllBytes(file.toPath()));
        email.setUpdateAttachment(updateAttachmentRepository.findTopByOrderByIdDesc());
        emailRepository.save(email);
    }

}
