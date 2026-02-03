package com.drivenow.service;

import com.drivenow.entity.Complaint;
import com.drivenow.entity.ComplaintReply;
import com.drivenow.repository.ComplaintRepository;
import com.drivenow.repository.ComplaintReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {
    
    private final ComplaintRepository complaintRepository;
    private final ComplaintReplyRepository complaintReplyRepository;
    
    public Complaint createComplaint(Complaint complaint) {
        log.info("Creating new complaint from: {}", complaint.getEmail());
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setStatus(Complaint.ComplaintStatus.OPEN);
        complaint.setUnreadCount(0);
        return complaintRepository.save(complaint);
    }
    
    public List<Complaint> getAllComplaints() {
        log.info("Fetching all complaints");
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Complaint> getComplaintsByEmail(String email) {
        log.info("Fetching complaints for email: {}", email);
        return complaintRepository.findByEmailIgnoreCase(email);
    }
    
    public List<Complaint> getComplaintsByStatus(Complaint.ComplaintStatus status) {
        log.info("Fetching complaints with status: {}", status);
        return complaintRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Transactional
    public Complaint closeComplaint(Long complaintId, String closedBy) {
        log.info("Closing complaint {} by {}", complaintId, closedBy);
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + complaintId));
        
        complaint.setStatus(Complaint.ComplaintStatus.CLOSED);
        complaint.setClosedAt(LocalDateTime.now());
        complaint.setClosedBy(closedBy);
        
        return complaintRepository.save(complaint);
    }
    
    @Transactional
    public ComplaintReply addReply(Long complaintId, ComplaintReply reply) {
        log.info("Adding reply to complaint {} by {}", complaintId, reply.getSenderType());
        
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + complaintId));
        
        reply.setComplaint(complaint);
        reply.setCreatedAt(LocalDateTime.now());
        
        // Increment unread count if reply is from user
        if ("USER".equals(reply.getSenderType())) {
            complaint.setUnreadCount(complaint.getUnreadCount() + 1);
            complaintRepository.save(complaint);
        }
        
        return complaintReplyRepository.save(reply);
    }
    
    public List<ComplaintReply> getComplaintReplies(Long complaintId) {
        log.info("Fetching replies for complaint: {}", complaintId);
        return complaintReplyRepository.findByComplaint_IdOrderByCreatedAtAsc(complaintId);
    }
    
    @Transactional
    public void markRepliesAsRead(Long complaintId) {
        log.info("Marking replies as read for complaint: {}", complaintId);
        
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + complaintId));
        
        complaint.setUnreadCount(0);
        complaintRepository.save(complaint);
        
        List<ComplaintReply> replies = complaintReplyRepository.findByComplaint_IdOrderByCreatedAtAsc(complaintId);
        replies.forEach(reply -> reply.setIsRead(true));
        complaintReplyRepository.saveAll(replies);
    }
}
