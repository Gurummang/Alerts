package com.GASB.alerts.service;

import jakarta.mail.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;


@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    public static SendRawEmailRequest getSendRawEmailRequest(String title, String content, String receiver, String fileRoot) throws MessagingException, IOException {

        // title : 메일 제목
        // content : 안에 내용
        // receiver : 받는 사람
        // fileRoot : 파일 경로

        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        // Define mail title
        message.setSubject(title);

        // Define mail Sender
        message.setFrom("gasb@grummang.com");

        // Define mail Receiver
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));

        // Create a multipart/mixed parent container.
        MimeMultipart msg = new MimeMultipart("mixed");

        // Define the text part.
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(content, "text/plain; charset=UTF-8");

        // Add the text part to the parent container.
        msg.addBodyPart(textPart);

        // Define the attachment
        MimeBodyPart att = new MimeBodyPart();
        if (fileRoot != null){
            DataSource fds = new FileDataSource(fileRoot);
            att.setDataHandler(new DataHandler(fds));
            att.setFileName(fds.getName());

            // Add the attachment to the message.
            msg.addBodyPart(att);
        }

        // Add the parent container to the message.
        message.setContent(msg);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
        return new SendRawEmailRequest(rawMessage);
    }
}

