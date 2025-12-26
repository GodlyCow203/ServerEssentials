package net.godlycow.org.mail.model;

import java.util.UUID;

public record MailMessage(
        UUID senderId,
        String senderName,
        String message,
        long timestamp
) {
    public static MailMessage create(UUID senderId, String senderName, String message) {
        return new MailMessage(senderId, senderName, message, System.currentTimeMillis());
    }
}