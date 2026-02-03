package com.drivenow.controller;

import com.drivenow.dto.MessageReplyDTO;
import com.drivenow.entity.ContactMessage;
import com.drivenow.entity.MessageReply;
import com.drivenow.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ContactMessage> createMessage(@RequestBody ContactMessage message) {
        try {
            log.info("Received contact message from: {}", message.getEmail());
            ContactMessage saved = contactMessageService.createMessage(message);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error creating contact message", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        try {
            List<ContactMessage> messages = contactMessageService.getAllMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching messages", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessage> getMessageById(@PathVariable Long id) {
        try {
            ContactMessage message = contactMessageService.getMessageById(id);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Error fetching message: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessage>> getMessagesByStatus(@PathVariable String status) {
        try {
            ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
            List<ContactMessage> messages = contactMessageService.getMessagesByStatus(messageStatus);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching messages by status: {}", status, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessage> updateMessageStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            ContactMessage.MessageStatus messageStatus = ContactMessage.MessageStatus.valueOf(status.toUpperCase());
            ContactMessage updated = contactMessageService.updateMessageStatus(id, messageStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating message status: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ContactMessage> replyToMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> reply) {
        try {
            String replyText = reply.get("replyText");
            String senderName = reply.get("senderName");
            String senderEmail = reply.get("senderEmail");
            
            log.info("Reply received for message {}: from {} ({})", id, senderName, senderEmail);
            
            ContactMessage updated = contactMessageService.replyToMessage(id, replyText, senderName, senderEmail);
            
            // Create notification for user if they are registered
            contactMessageService.createReplyNotificationForUser(updated);
            
            // Log for email notification (future enhancement for non-registered users)
            log.info("Reply sent to message ID: {}. Email: {}. User notified.", 
                id, updated.getEmail());
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error replying to message: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        try {
            contactMessageService.deleteMessage(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting message: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/count/new")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getNewMessageCount() {
        try {
            Long count = contactMessageService.getNewMessageCount();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error fetching new message count", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<ContactMessage>> getMessagesByUserEmail(@PathVariable String email) {
        try {
            List<ContactMessage> messages = contactMessageService.getMessagesByEmail(email);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching messages for email: {}", email, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessage>> getMessagesByType(@PathVariable String type) {
        try {
            ContactMessage.MessageType messageType = ContactMessage.MessageType.valueOf(type.toUpperCase());
            List<ContactMessage> messages = contactMessageService.getMessagesByType(messageType);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching messages by type: {}", type, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{email}/type/{type}")
    public ResponseEntity<List<ContactMessage>> getMessagesByEmailAndType(
            @PathVariable String email, 
            @PathVariable String type) {
        try {
            ContactMessage.MessageType messageType = ContactMessage.MessageType.valueOf(type.toUpperCase());
            List<ContactMessage> messages = contactMessageService.getMessagesByEmailAndType(email, messageType);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching messages for email {} and type {}", email, type, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<MessageReplyDTO>> getMessageReplies(@PathVariable Long id) {
        try {
            log.info("Fetching replies for message ID: {}", id);
            List<MessageReply> replies = contactMessageService.getMessageReplies(id);
            log.info("Found {} replies for message {}", replies.size(), id);
            
            // Convert to DTOs to avoid serialization issues
            List<MessageReplyDTO> replyDTOs = replies.stream()
                .map(reply -> {
                    log.info("Processing Reply ID: {}, SenderType: {}, SenderName: {}, SenderEmail: {}", 
                        reply.getId(), reply.getSenderType(), reply.getSenderName(), reply.getSenderEmail());
                    return MessageReplyDTO.fromEntity(reply);
                })
                .collect(Collectors.toList());
            
            log.info("Successfully converted {} replies to DTOs", replyDTOs.size());
            return ResponseEntity.ok(replyDTOs);
        } catch (Exception e) {
            log.error("Error fetching replies for message ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessage> resolveMessage(@PathVariable Long id) {
        try {
            log.info("Resolving message ID: {}", id);
            ContactMessage updated = contactMessageService.updateMessageStatus(id, ContactMessage.MessageStatus.RESOLVED);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error resolving message: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
