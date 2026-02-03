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
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final MessageReplyRepository messageReplyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ContactMessage createMessage(ContactMessage message) {
        log.info("Creating new contact message from: {}", message.getEmail());
        message.setStatus(ContactMessage.MessageStatus.NEW);
        ContactMessage savedMessage = contactMessageRepository.save(message);
        
        // Create notification for all admins
        createContactMessageNotificationForAdmins(savedMessage);
        
        return savedMessage;
    }

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public ContactMessage getMessageById(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
    }

    public List<ContactMessage> getMessagesByStatus(ContactMessage.MessageStatus status) {
        return contactMessageRepository.findByStatus(status);
    }

    @Transactional
    public ContactMessage updateMessageStatus(Long id, ContactMessage.MessageStatus status) {
        ContactMessage message = getMessageById(id);
        message.setStatus(status);
        log.info("Updated message {} status to: {}", id, status);
        return contactMessageRepository.save(message);
    }

    @Transactional
    public ContactMessage replyToMessage(Long id, String replyText, String senderName, String senderEmail) {
        ContactMessage message = getMessageById(id);
        
        // Check if message is resolved
        if (message.getStatus() == ContactMessage.MessageStatus.RESOLVED) {
            throw new RuntimeException("Cannot reply to a resolved message");
        }
        
        log.info("Processing reply for message {}: Original sender: {}, Reply sender: {}", 
            id, message.getEmail(), senderEmail);
        
        // Create a new reply entry
        MessageReply reply = new MessageReply();
        reply.setMessageId(id);
        reply.setReplyText(replyText);
        reply.setSenderName(senderName);
        reply.setSenderEmail(senderEmail);
        
        // Determine sender type based on email or role
        // If sender email matches the original message sender, it's USER, otherwise ADMIN
        if (message.getEmail().equalsIgnoreCase(senderEmail)) {
            reply.setSenderType("USER");
            log.info("Sender type determined as USER (email matches original sender)");
        } else {
            reply.setSenderType("ADMIN");
            log.info("Sender type determined as ADMIN (email does not match original sender)");
        }
        
        reply.setCreatedAt(LocalDateTime.now());
        reply.setIsRead(false);
        
        MessageReply savedReply = messageReplyRepository.save(reply);
        log.info("Reply saved with ID: {}, Type: {}, Sender: {}", 
            savedReply.getId(), savedReply.getSenderType(), savedReply.getSenderName());
        
        // Update message status and replied timestamp
        message.setRepliedAt(LocalDateTime.now());
        message.setStatus(ContactMessage.MessageStatus.REPLIED);
        
        log.info("{} replied to message: {} - Reply ID: {}", 
            reply.getSenderType(), id, savedReply.getId());
        return contactMessageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long id) {
        log.info("Deleting message: {}", id);
        contactMessageRepository.deleteById(id);
    }

    public Long getNewMessageCount() {
        return contactMessageRepository.countByStatus(ContactMessage.MessageStatus.NEW);
    }

    public List<ContactMessage> getMessagesByEmail(String email) {
        return contactMessageRepository.findByEmailIgnoreCase(email);
    }
    
    public List<ContactMessage> getMessagesByType(ContactMessage.MessageType type) {
        return contactMessageRepository.findAll().stream()
            .filter(msg -> msg.getMessageType() == type)
            .sorted((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()))
            .toList();
    }
    
    public List<ContactMessage> getMessagesByEmailAndType(String email, ContactMessage.MessageType type) {
        return contactMessageRepository.findByEmailIgnoreCase(email).stream()
            .filter(msg -> msg.getMessageType() == type)
            .toList();
    }
    
    /**
     * Get all replies for a specific message
     */
    public List<MessageReply> getMessageReplies(Long messageId) {
        return messageReplyRepository.findByMessageIdOrderByCreatedAtAsc(messageId);
    }
    
    /**
     * Create notification for all admins when a new contact message arrives
     */
    private void createContactMessageNotificationForAdmins(ContactMessage message) {
        try {
            // Get all admin users
            List<User> admins = userRepository.findByRole(User.Role.ADMIN);
            
            for (User admin : admins) {
                Notification notification = new Notification();
                notification.setUser(admin);
                notification.setTitle("📩 New Contact Message");
                notification.setMessage(String.format("New message from %s: %s", 
                    message.getName(), message.getSubject()));
                notification.setType(Notification.NotificationType.INFO);
                notification.setCategory(Notification.NotificationCategory.MESSAGE);
                notification.setIsRead(false);
                notification.setActionUrl("/dashboard/admin?tab=messages");
                
                // Save without authentication check (system-generated notification)
                notificationService.createSystemNotification(notification);
                log.info("Created contact message notification for admin: {}", admin.getEmail());
            }
        } catch (Exception e) {
            log.error("Error creating contact message notifications for admins", e);
        }
    }
    
    /**
     * Create notification for user when admin replies (if user is registered)
     */
    public void createReplyNotificationForUser(ContactMessage message) {
        try {
            // Check if the email belongs to a registered user
            userRepository.findByEmail(message.getEmail()).ifPresent(user -> {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setTitle("💬 Admin Reply Received");
                notification.setMessage(String.format("Admin replied to your message: %s", 
                    message.getSubject()));
                notification.setType(Notification.NotificationType.SUCCESS);
                notification.setCategory(Notification.NotificationCategory.MESSAGE);
                notification.setIsRead(false);
                notification.setActionUrl("/dashboard/user?tab=messages");
                
                // Save without authentication check (system-generated notification)
                notificationService.createSystemNotification(notification);
                log.info("Created reply notification for user: {}", user.getEmail());
            });
        } catch (Exception e) {
            log.error("Error creating reply notification for user", e);
        }
    }
}
