package com.notifplatform.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import com.notifplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private final UserRepository userRepository;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${mail.from}")
    private String fromEmail;

    public EmailSender(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void send(Long userId, String message) throws Exception {
        String recipientEmail = userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId));

        log.info("━━━ EMAIL ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  To      : {}", recipientEmail);
        log.info("  Message : {}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Email from = new Email(fromEmail);
        Email to = new Email(recipientEmail);
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, "Notification", to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid error: " + response.getStatusCode()
                    + " " + response.getBody());
        }

        log.info("✓ Email sent via SendGrid — status: {}", response.getStatusCode());
    }
}
