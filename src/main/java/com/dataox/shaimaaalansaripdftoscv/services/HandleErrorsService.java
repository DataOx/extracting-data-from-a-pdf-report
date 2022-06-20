package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@AllArgsConstructor
public class HandleErrorsService {
    private final EmailRepository emailRepository;
    private final SendingEmailsService sendingEmailsService;

    public void checkThatEmailHasErrorWhileSending(EmailEntity email) {
        email.setHasSendingError(true);
        emailRepository.save(email);
    }

    @Scheduled(fixedRate = 100000000)
    public void resendEmail() {
        try {
            List<String> attachmentNames = new ArrayList<>();
            for (EmailEntity email : emailRepository.findAllByHasSendingErrorIsTrue()) {
                email.setHasSendingError(false);
                emailRepository.save(email);
                attachmentNames.add(email.updateAttachment.name);
            }
            sendingEmailsService.sendEmailToClient(attachmentNames);
        } catch (Exception e) {
            log.info("Can't resend emails.");
        }
    }

    private void resendEmailTharReceivedAsSpam(LocalDate date) throws Exception {
        List<String> attachmentNames = new ArrayList<>();
        for (EmailEntity email : emailRepository.findAllBySendingTimeGreaterThan(date)) {
            emailRepository.save(email);
            attachmentNames.add(email.updateAttachment.name);
        }
        sendingEmailsService.sendEmailToClient(attachmentNames);
    }

}