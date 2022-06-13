package com.dataox.shaimaaalansaripdftoscv.config;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.AttachmentCollectionRequest;
import com.microsoft.graph.requests.MessageCollectionPage;
import lombok.AllArgsConstructor;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@AllArgsConstructor
public class SubscribeConfig {
    private final GraphConfig config;

    public static void initializeGraph() {
        try {
            GraphConfig.initializeGraphForUserAuth(getProperties(), challenge -> System.out.println(challenge.getMessage()));
        } catch (Exception e) {
            System.out.println("Error initializing Graph for user auth:");
            System.out.println(e.getMessage());
        }
    }

    public static List<Attachment> listInbox() throws Exception {
        try {
            final MessageCollectionPage messages = GraphConfig.getInbox();

            List<Attachment> attachments = new ArrayList<>();

            for (Message message : messages.getCurrentPage()) {
                assert message.from != null;
                assert message.from.emailAddress != null;
                assert message.receivedDateTime != null;

                System.out.println("Message: " + message.subject);
                System.out.println("  From: " + message.from.emailAddress.name);
                System.out.println("  Received: " + message.receivedDateTime
                        .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));

                AttachmentCollectionRequest request = GraphConfig.getUserClient().me().messages(message.id).attachments().buildRequest();

                if (Boolean.TRUE.equals(message.hasAttachments)) {
                    attachments.add(Objects.requireNonNull(request.get()).getCurrentPage().get(0));
                }
            }

            final boolean moreMessagesAvailable = messages.getNextPage() != null;
            System.out.println("More messages available? " + moreMessagesAvailable);

            return attachments;
        } catch (Exception e) {
            System.out.println("Error getting inbox");
            System.out.println(e.getMessage());
            throw e;
        }
    }

    private static Properties getProperties() throws IOException {
        final Properties properties = new Properties();

        try {
            File file = ResourceUtils.getFile("classpath:oAuth.properties");
            properties.load(Files.newInputStream(file.toPath()));

            return properties;
        } catch (IOException e) {
            System.out.println("Unable to read OAuth configuration.");
            throw e;
        }
    }


    public void greetUser() {
        try {
            final User user = config.getUser();
            final String email = user.mail == null ? user.userPrincipalName : user.mail;
            System.out.println("Going to read " + user.displayName + "'s emails from inbox of " + email);
        } catch (Exception e) {
            System.out.println("Error getting user:");
            System.out.println(e.getMessage());
        }
    }

    private void sendMail() {
        try {
            final User user = config.getUser();
            final String email = user.mail == null ? user.userPrincipalName : user.mail;

            config.sendMail("Testing Microsoft Graph", "Hello world!", email);
            System.out.println("\nMail sent.");
        } catch (Exception e) {
            System.out.println("Error sending mail");
            System.out.println(e.getMessage());
        }
    }

}
