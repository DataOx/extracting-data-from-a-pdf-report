package com.dataox.shaimaaalansaripdftoscv.config;

import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.AttachmentCollectionRequest;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageCollectionPage;
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

    public static void initializeGraph() {
        try {
            initializeGraphForUserAuth(getProperties(), challenge -> System.out.println(challenge.getMessage()));
        } catch (Exception e) {
            log.info("Error initializing Graph for user auth:");
            log.info(e.getMessage());
        }
    }

    public static List<Attachment> listInbox() throws Exception {
        try {
            final MessageCollectionPage messages = getInbox();
            List<Attachment> attachments = new ArrayList<>();

            for (Message message : messages.getCurrentPage()) {
                AttachmentCollectionRequest request = _userClient.me().messages(message.id).attachments().buildRequest();
                if (message.hasAttachments) {
                    attachments.add(Objects.requireNonNull(request.get()).getCurrentPage().get(0));
                }
            }
            return attachments;
        } catch (Exception e) {
            log.info("Щось сталося");
            throw e;
        }
    }

    public void sendMail() {
        try {
            final User user = getUser();
            final String email = user.mail == null ? user.userPrincipalName : user.mail;
            sendMail("Testing Microsoft Graph", "Hello world!", email);
            log.info("\nMail sent.");
        } catch (Exception e) {
            log.info("Error sending mail");
            log.info(e.getMessage());
        }
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

    private static MessageCollectionPage getInbox() throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth");
        }

        return _userClient.me()
                .mailFolders("inbox")
                .messages()
                .buildRequest()
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

    private User getUser() throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth.");
        }

        return _userClient.me()
                .buildRequest()
                .select("displayName,mail,userPrincipalName")
                .get();
    }

    private void sendMail(String subject, String body, String recipient) throws Exception {
        // Ensure client isn't null
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth");
        }

        // Create a new message
        final Message message = new Message();
        message.subject = subject;
        message.body = new ItemBody();
        message.body.content = body;
        message.body.contentType = BodyType.TEXT;

        final Recipient toRecipient = new Recipient();
        toRecipient.emailAddress = new EmailAddress();
        toRecipient.emailAddress.address = recipient;
        message.toRecipients = List.of(toRecipient);

        // Send the message
        _userClient.me()
                .sendMail(UserSendMailParameterSet.newBuilder()
                        .withMessage(message)
                        .build())
                .buildRequest()
                .post();
    }

}
