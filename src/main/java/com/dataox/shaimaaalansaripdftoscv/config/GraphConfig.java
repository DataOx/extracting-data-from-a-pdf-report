package com.dataox.shaimaaalansaripdftoscv.config;

import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.httpcore.AuthenticationHandler;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.httpcore.RedirectHandler;
import com.microsoft.graph.httpcore.RetryHandler;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.*;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.nio.file.Files.writeString;
import static java.nio.file.Path.of;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Log4j2
public class GraphConfig {

    private final static String CONNECT_INFO_MSG_TEMPLATE =
            "%s\nUrl: %s\nCode: %s\nExpires on: %s";

    private final static Path CONNECT_INFO_PATH = of("connect_info.txt");

    private static GraphServiceClient<Request> _userClient;

    public static void initializeGraphAccount() throws Exception {
        try {
            initializeGraphForUserAuth(getProperties(), challenge -> {
                try {
                    writeString(
                            CONNECT_INFO_PATH,
                            format(
                                    CONNECT_INFO_MSG_TEMPLATE,
                                    challenge.getMessage(),
                                    challenge.getVerificationUrl(),
                                    challenge.getUserCode(),
                                    challenge.getExpiresOn()
                            ),
                            CREATE, TRUNCATE_EXISTING);
                    log.info("CONNECT INFO UPDATED. SEE connect_info.txt");
                } catch (IOException e) {
                    throw new RuntimeException("Connect info not handled");
                }
            });
        } catch (Exception e) {
            log.info("Error initializing Graph for user auth:");
            throw e;
        }
    }

    public static List<Attachment> getListOfEmailsAttachmentsThatReceiveLaterThenLastSaved(LocalDateTime date) throws Exception {
        try {
            final MessageCollectionPage messagesFromInbox = getInboxMessages(String.valueOf(date).substring(0, 19) + "Z");
            List<Attachment> attachments = new ArrayList<>();
            for (Message message : messagesFromInbox.getCurrentPage()) {
                AttachmentCollectionRequest request = _userClient.me().messages(message.id).attachments().buildRequest();
                if (message.hasAttachments) {
                    attachments.add(Objects.requireNonNull(request.get()).getCurrentPage().get(0));
                }
            }
            return attachments;
        } catch (Exception e) {
            log.info("Can't receive emails. " + e);
            throw e;
        }
    }

    public static void sendEmail(String subject, String body, AttachmentCollectionResponse attachment, List<String> recipientsEmailAddress) throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth.");
        }

        final Message message = new Message();
        message.subject = subject;
        message.body = new ItemBody();
        message.body.content = body;
        message.body.contentType = BodyType.TEXT;
        message.attachments = new AttachmentCollectionPage(attachment, null);
        message.hasAttachments = true;

        List<Recipient> recipients = new ArrayList<>();
        for (String recipientEmailAddress : recipientsEmailAddress) {
            final Recipient recipient = new Recipient();
            recipient.emailAddress = new EmailAddress();
            recipient.emailAddress.address = recipientEmailAddress;
            recipients.add(recipient);
        }
        message.toRecipients = recipients;

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
        final List<String> graphUserScopes = Arrays.asList(properties.getProperty("app.graphUserScopes").split(","));
        DeviceCodeCredential _deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .clientId(clientId)
                .tenantId(authTenantId)
                .challengeConsumer(challenge)
                .build();
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(graphUserScopes, _deviceCodeCredential);
        OkHttpClient client = HttpClients.custom()
                .addInterceptor(new AuthenticationHandler(authProvider))
                .addInterceptor(new RetryHandler())
                .addInterceptor(new RedirectHandler())
                .connectTimeout(Duration.ofMinutes(1))
                .build();

        _userClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .httpClient(client)
                .buildClient();
    }

    private static MessageCollectionPage getInboxMessages(String timeForReceiveMessage) throws Exception {
        if (_userClient == null) {
            throw new Exception("Graph has not been initialized for user auth.");
        }

        return _userClient.me()
                .mailFolders("inbox")
                .messages()
                .buildRequest()
                .filter("receivedDateTime ge " + timeForReceiveMessage)
                .orderBy("receivedDateTime ASC")
                .get();
    }

    private static Properties getProperties() throws IOException {
        final Properties properties = new Properties();

        try {
            File file = ResourceUtils.getFile("config" + File.separator + "oAuth.properties");
            properties.load(Files.newInputStream(file.toPath()));

            return properties;
        } catch (IOException e) {
            log.info("Unable to read OAuth configuration.");
            throw e;
        }
    }

}
