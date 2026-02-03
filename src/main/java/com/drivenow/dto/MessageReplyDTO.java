package com.drivenow.dto;

import com.drivenow.entity.MessageReply;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReplyDTO {
    private Long id;
    private String replyText;
    private String senderType;
    private String senderName;
    private String senderEmail;
    private LocalDateTime createdAt;
    private boolean isRead;
    
    // Convert entity to DTO
    public static MessageReplyDTO fromEntity(MessageReply entity) {
        MessageReplyDTO dto = new MessageReplyDTO();
        dto.setId(entity.getId());
        dto.setReplyText(entity.getReplyText());
        dto.setSenderType(entity.getSenderType());
        dto.setSenderName(entity.getSenderName());
        dto.setSenderEmail(entity.getSenderEmail());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setRead(entity.getIsRead() != null ? entity.getIsRead() : false);
        return dto;
    }
}
