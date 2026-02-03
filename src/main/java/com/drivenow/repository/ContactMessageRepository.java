package com.drivenow.repository;

import com.drivenow.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findByStatus(ContactMessage.MessageStatus status);
    List<ContactMessage> findByEmailIgnoreCase(String email);
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    Long countByStatus(ContactMessage.MessageStatus status);
}
