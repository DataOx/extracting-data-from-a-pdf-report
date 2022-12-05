package com.dataox.shaimaaalansaripdftoscv.config;

import com.dataox.shaimaaalansaripdftoscv.domain.ConvertData;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.dataox.shaimaaalansaripdftoscv.services.ConvertService;
import com.dataox.shaimaaalansaripdftoscv.services.ReceivingFilesService;
import com.dataox.shaimaaalansaripdftoscv.services.SendingEmailsService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Log4j2
@Component
@AllArgsConstructor
public class SchedulingConfig {
    private final ReceivingFilesService receivingFilesService;
    private final SendingEmailsService sendingEmailsService;
    private final ConvertService convertService;
    private final EmailRepository emailRepository;


    @Scheduled(cron = "${morning.scheduler}")
    @Scheduled(cron = "${day.scheduler}")
    public void prepareToSendingEmails() {
        log.info("Start to create PDFs.");
        List<EmailEntity> emails = emailRepository.findAllByHandledIsFalseAndUpdateAttachmentNotNull();
        if (isEmpty(emails)) {
            log.info("No emails.");
            return;
        }
        emails = checkIfEmailsHasTodayDateInName(emails);
        ConvertData emailsWithAttachments = convertService.createPdfFiles(emails);
        if (!isEmpty(emailsWithAttachments.getAttachments())) {
            sendingEmailsService.createEmailAndSendToClient(emailsWithAttachments.getAttachments());
            log.info("Start to send email.");
        }
    }

    private List<EmailEntity> checkIfEmailsHasTodayDateInName(List<EmailEntity> emails) {
        String dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        return emails.stream().filter(email -> email.getUpdateAttachment().getName().contains(dateToday)).toList();
    }

    @Scheduled(fixedRate = 100000, initialDelay = 1000)
    public void receiveAttachments() {
        receivingFilesService.receiveAttachments();
    }

}
