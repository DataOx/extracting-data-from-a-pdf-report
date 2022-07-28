package com.dataox.shaimaaalansaripdftoscv.services;

import com.google.common.io.Files;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

@Log4j2
@Service
public class SendingEmailsService {

    @Value("${email.recipient}")
    private String recipientEmails;
    @Value("${email.user}")
    private String username;
    @Value("${email.password}")
    private String password;
    final String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));


    public boolean isEmailCreatedAndSendToClient(Map<String, byte[]> attachments) {
        try {
            sendEmail(attachments);
            return true;
        } catch (Exception e) {
            log.info("Error sending mail: ");
            log.info(e.getMessage());
            return false;
        }
    }

    public void sendEmail(Map<String, byte[]> files) {
        try {
            Message message = new MimeMessage(startSession());
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmails));
            message.setSubject("NP Report" + date);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (String key : files.keySet()) {
                messageBodyPart = new MimeBodyPart();
                Files.write(files.get(key), new File("./" + key));
                messageBodyPart.setDataHandler(new DataHandler(new FileDataSource("./" + key)));
                messageBodyPart.setFileName(key);
                multipart.addBodyPart(messageBodyPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            log.info("Sent message successfully.");
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Session startSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.timeout", 1000);
        props.put("mail.smtp.connectiontimeout", 1000);
        props.put("mail.smtp.ssl.protocols", "TLSv1.3");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }


}
