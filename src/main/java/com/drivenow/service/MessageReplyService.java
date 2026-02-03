package com.drivenow.service;

import com.drivenow.entity.ContactMessage;
import com.drivenow.entity.MessageReply;
import com.drivenow.entity.Notification;
import com.drivenow.entity.User;
import com.drivenow.repository.ContactMessageRepository;
import com.drivenow.repository.MessageReplyRepository;
import com.drivenow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageReplyService {

    private final MessageReplyRepository messageReplyRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public MessageReply addReply(Long messageId, String replyText, String senderType, String senderName, String senderEmail) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        MessageReply reply = new MessageReply();
        reply.setMessageId(messageId);
        reply.setReplyText(replyText);
        reply.setSenderType(senderType);
        reply.setSenderName(senderName);
        reply.setSenderEmail(senderEmail);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setIsRead(false);

        MessageReply savedReply = messageReplyRepository.save(reply);
        
        // Update message status
        message.setStatus(ContactMessage.MessageStatus.REPLIED);
        contactMessageRepository.save(message);

        // Create notifications
        if ("ADMIN".equals(senderType)) {
            createNotificationForUser(message, replyText);
        } else {
            createNotificationForAdmins(message, senderName, replyText);
        }

        log.info("Reply added to message {} by {}", messageId, senderType);
        return savedReply;
    }

    public List<MessageReply> getRepliesByMessageId(Long messageId) {
        try {
            log.info("Fetching replies for message ID: {}", messageId);
            List<MessageReply> replies = messageReplyRepository.findByMessageIdOrderByCreatedAtAsc(messageId);
            log.info("Found {} replies for message ID: {}", replies != null ? replies.size() : 0, messageId);
            return replies != null ? replies : List.of();
        } catch (Exception e) {
            log.error("Error in getRepliesByMessageId for message {}: {}", messageId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch replies for message: " + messageId, e);
        }
    }

    @Transactional
    public void markRepliesAsRead(Long messageId) {
        List<MessageReply> replies = messageReplyRepository.findByMessageIdOrderByCreatedAtAsc(messageId);
        replies.forEach(reply -> reply.setIsRead(true));
        messageReplyRepository.saveAll(replies);
    }

    private void createNotificationForUser(ContactMessage message, String replyText) {
        try {
            userRepository.findByEmail(message.getEmail()).ifPresent(user -> {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setTitle("💬 New Reply from Admin");
                notification.setMessage(String.format("New reply on: %s", message.getSubject()));
                notification.setType(Notification.NotificationType.SUCCESS);
                notification.setCategory(Notification.NotificationCategory.SYSTEM);
                notification.setIsRead(false);
                notification.setActionUrl("/dashboard?tab=profile");

                notificationService.createSystemNotification(notification);
                log.info("Created reply notification for user: {}", user.getEmail());
            });
        } catch (Exception e) {
            log.error("Error creating notification for user", e);
        }
    }

    private void createNotificationForAdmins(ContactMessage message, String userName, String replyText) {
        try {
            List<User> admins = userRepository.findByRole(User.Role.ADMIN);
            
            for (User admin : admins) {
                Notification notification = new Notification();
                notification.setUser(admin);
                notification.setTitle("💬 New User Reply");
                notification.setMessage(String.format("%s replied to: %s", userName, message.getSubject()));
                notification.setType(Notification.NotificationType.INFO);
                notification.setCategory(Notification.NotificationCategory.SYSTEM);
                notification.setIsRead(false);
                notification.setActionUrl("/admin/dashboard?tab=messages");

                notificationService.createSystemNotification(notification);
                log.info("Created reply notification for admin: {}", admin.getEmail());
            }
        } catch (Exception e) {
            log.error("Error creating notifications for admins", e);
        }
    }
}
