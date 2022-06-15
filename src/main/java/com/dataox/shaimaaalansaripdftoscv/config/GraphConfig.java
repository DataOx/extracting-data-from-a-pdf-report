package com.dataox.shaimaaalansaripdftoscv.config;

import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.*;
import lombok.extern.log4j.Log4j2;
import okhttp3.Request;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

@Log4j2
public class GraphConfig {
    private static GraphServiceClient<Request> _userClient;

    public static void initializeGraph() throws Exception {
        try {
            initializeGraphForUserAuth(getProperties(), challenge -> System.out.println(challenge.getMessage()));
        } catch (Exception e) {
            log.info("Error initializing Graph for user auth:");
            throw e;
        }
    }

    public static List<Attachment> getAttachmentsList() throws Exception {
        try {
            final MessageCollectionPage messagesFromInbox = getInboxMessages();
            List<Attachment> attachments = new ArrayList<>();
            for (Message message : messagesFromInbox.getCurrentPage()) {
                AttachmentCollectionRequest request = _userClient.me().messages(message.id).attachments().buildRequest();
                if (message.hasAttachments) {
                    Attachment attachment = Objects.requireNonNull(request.get()).getCurrentPage().get(0);
                    attachments.add(attachment);
                    log.info("Receive email with attachment '" + attachment.name);
                }
            }
            return attachments;
        } catch (Exception e) {
            log.info("Can't receive emails.");
            throw e;
        }
    }

    public static User getRecipientUser() throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth.");
        }

        return _userClient.me()
                .buildRequest()
                .select("displayName,mail,userPrincipalName")
                .get();
    }

    public static void sendMail(String subject, String body, AttachmentCollectionResponse attachment, String recipient) throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth");
        }

        final Message message = new Message();
        message.subject = subject;
        message.body = new ItemBody();
        message.body.content = body;
        message.body.contentType = BodyType.TEXT;
        message.attachments = new AttachmentCollectionPage(attachment, null);
        message.hasAttachments = true;

        final Recipient toRecipient = new Recipient();
        toRecipient.emailAddress = new EmailAddress();
        toRecipient.emailAddress.address = recipient;
        message.toRecipients = List.of(toRecipient);

        _userClient.me()
                .sendMail(UserSendMailParameterSet.newBuilder().withMessage(message).build())
                .buildRequest()
                .post();
    }


    private static void initializeGraphForUserAuth(Properties properties, Consumer<DeviceCodeInfo> challenge) throws Exception {
        if (properties == null) {
            throw new Exception("Properties cannot be null");
        }

        final String clientId = properties.getProperty("app.clientId");
        final String authTenantId = properties.getProperty("app.authTenant");
        final List<String> graphUserScopes = Arrays
                .asList(properties.getProperty("app.graphUserScopes").split(","));

        DeviceCodeCredential _deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .clientId(clientId)
                .tenantId(authTenantId)
                .challengeConsumer(challenge)
                .build();

        final TokenCredentialAuthProvider authProvider =
                new TokenCredentialAuthProvider(graphUserScopes, _deviceCodeCredential);

        _userClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    private static MessageCollectionPage getInboxMessages() throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth");
        }

        return _userClient.me()
                .mailFolders("inbox")
                .messages()
                .buildRequest()
                .filter("receivedDateTime ge 2022-06-10T00:00:01Z")
                .orderBy("receivedDateTime DESC")
                .get();
    }

    private static Properties getProperties() throws IOException {
        final Properties properties = new Properties();

        try {
            File file = ResourceUtils.getFile("classpath:oAuth.properties");
            properties.load(Files.newInputStream(file.toPath()));

            return properties;
        } catch (IOException e) {
            log.info("Unable to read OAuth configuration.");
            throw e;
        }
    }

}
