package com.dataox.shaimaaalansaripdftoscv.services;

import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class SendingErrorsHandlerService {

    private final EmailRepository emailRepository;

    public void checkThatEmailHasErrorWhileSending(EmailEntity email) {
        email.setHasSendingError(true);
        emailRepository.save(email);
    }

    public void resendEmail(EmailEntity email) {
        email.setHasSendingError(false);
        email.setHandled(false);
        emailRepository.save(email);
    }

}
