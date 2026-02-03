package com.drivenow.repository;

import com.drivenow.entity.ComplaintReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintReplyRepository extends JpaRepository<ComplaintReply, Long> {
    List<ComplaintReply> findByComplaint_IdOrderByCreatedAtAsc(Long complaintId);
    List<ComplaintReply> findByComplaint_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
