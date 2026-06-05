package com.library.service;

import com.library.model.Notification;
import com.library.model.NotificationType;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService
{
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    @Async
    public void sendNotificationEmail(Notification notification)
    {
        if (notification == null || notification.getUser() == null
                || notification.getUser().getEmail() == null)
        {
            return;
        }

        String to = notification.getUser().getEmail();
        String subject = subjectFor(notification.getType());
        String html = buildHtml(notification);

        try
        {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email notification sent to {} (type={})", to, notification.getType());
        }
        catch (Exception e)
        {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String subjectFor(NotificationType type)
    {
        if (type == null) return "Bibliotheca — notificare";

        return switch (type)
        {
            case BOOK_AVAILABLE          -> "Bibliotheca — carte disponibilă";
            case DUE_SOON                -> "Bibliotheca — termen de returnare aproape";
            case OVERDUE                 -> "Bibliotheca — împrumut expirat";
            case RESERVATION_CONFIRMED   -> "Bibliotheca — rezervare confirmată";
            case RESERVATION_WAITING     -> "Bibliotheca — pe lista de așteptare";
            case RESERVATION_CANCELLED   -> "Bibliotheca — rezervare anulată";
            case RESERVATION_EXPIRED     -> "Bibliotheca — rezervare expirată";
            case READY_FOR_PICKUP        -> "Bibliotheca — cartea ta e gata de ridicat";
            case BOOK_BORROWED           -> "Bibliotheca — împrumut confirmat";
            case BOOK_RETURNED           -> "Bibliotheca — returnare înregistrată";
            case WAITING_LIST_AVAILABLE  -> "Bibliotheca — o copie e disponibilă pentru tine";
            case SYSTEM                  -> "Bibliotheca — mesaj de sistem";
            case GENERAL                 -> "Bibliotheca — notificare";
        };
    }

    private String buildHtml(Notification n)
    {
        String userName = n.getUser().getFullName() != null
                ? n.getUser().getFullName().split(" ")[0]
                : "cititorule";
        String typeLabel = n.getType() != null
                ? n.getType().name().replace("_", " ").toLowerCase()
                : "notificare";

        return """
            <div style="font-family:Georgia,serif;max-width:560px;margin:0 auto;padding:32px;background:#f7f1e7;color:#171717;">
              <h2 style="font-style:italic;font-weight:normal;margin:0 0 8px;">Bună, %s.</h2>
              <p style="font-size:12px;letter-spacing:2px;text-transform:uppercase;opacity:0.6;margin:0 0 24px;">%s</p>
              <div style="background:#fff;padding:24px;border:1px solid #e6dccb;border-radius:8px;">
                <p style="font-size:16px;line-height:1.6;margin:0;">%s</p>
              </div>
              <p style="margin-top:24px;font-size:12px;opacity:0.6;">— Echipa Bibliotheca</p>
            </div>
            """.formatted(
                escape(userName),
                escape(typeLabel),
                escape(n.getMessage())
        );
    }

    private String escape(String s)
    {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}