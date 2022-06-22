package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Log4j2
@Service
@AllArgsConstructor
public class HandleErrorsService {
    private final EmailRepository emailRepository;
    private final SendingEmailsService sendingEmailsService;

    public void checkThatEmailHasErrorWhileSending(List<EmailEntity> emails) {
        emails.forEach(email -> {
            email.setHasSendingError(true);
            email.setHandled(true);
            emailRepository.save(email);
        });
    }

    @Scheduled(cron = "${morning.scheduler}")
    @Scheduled(cron = "${day.scheduler}")
    public void resendEmail() {
        List<EmailEntity> emailEntities = emailRepository.findAllByHasSendingErrorIsTrue();
        if (!emailEntities.isEmpty()) {
            LocalDateTime now = now();
            try {
                List<String> attachmentNames = new ArrayList<>();
                for (EmailEntity email : emailEntities) {
                    email.setHasSendingError(false);
                    email.setSendingTime(now);
                    emailRepository.save(email);
                    attachmentNames.add(email.updateAttachment.name);
                }
                sendingEmailsService.sendEmailToClient(attachmentNames);
            } catch (Exception e) {
                log.info("Can't resend emails.");
            }
        }
    }

}