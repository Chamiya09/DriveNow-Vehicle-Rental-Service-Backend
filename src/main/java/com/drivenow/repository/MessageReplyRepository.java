package com.drivenow.repository;

import com.drivenow.entity.MessageReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageReplyRepository extends JpaRepository<MessageReply, Long> {
    List<MessageReply> findByMessageIdOrderByCreatedAtAsc(Long messageId);
    Long countByMessageIdAndIsReadFalse(Long messageId);
}
