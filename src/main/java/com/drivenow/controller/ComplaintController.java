package com.drivenow.controller;

import com.drivenow.entity.Complaint;
import com.drivenow.entity.ComplaintReply;
import com.drivenow.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintController {
    
    private final ComplaintService complaintService;
    
    @PostMapping
    public ResponseEntity<?> createComplaint(@RequestBody Complaint complaint) {
        try {
            log.info("Received complaint from: {}", complaint.getEmail());
            Complaint savedComplaint = complaintService.createComplaint(complaint);
            return ResponseEntity.ok(savedComplaint);
        } catch (Exception e) {
            log.error("Error creating complaint: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create complaint: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllComplaints() {
        try {
            log.info("Fetching all complaints");
            List<Complaint> complaints = complaintService.getAllComplaints();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            log.error("Error fetching complaints: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch complaints: " + e.getMessage()));
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getComplaintsByEmail(@PathVariable String email) {
        try {
            log.info("Fetching complaints for email: {}", email);
            List<Complaint> complaints = complaintService.getComplaintsByEmail(email);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            log.error("Error fetching complaints by email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch complaints: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getComplaintsByStatus(@PathVariable String status) {
        try {
            log.info("Fetching complaints with status: {}", status);
            Complaint.ComplaintStatus complaintStatus = Complaint.ComplaintStatus.valueOf(status.toUpperCase());
            List<Complaint> complaints = complaintService.getComplaintsByStatus(complaintStatus);
            return ResponseEntity.ok(complaints);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid status. Use OPEN or CLOSED"));
        } catch (Exception e) {
            log.error("Error fetching complaints by status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch complaints: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{complaintId}/close")
    public ResponseEntity<?> closeComplaint(
            @PathVariable Long complaintId,
            @RequestBody Map<String, String> request) {
        try {
            String closedBy = request.getOrDefault("closedBy", "Admin");
            log.info("Closing complaint {} by {}", complaintId, closedBy);
            Complaint closedComplaint = complaintService.closeComplaint(complaintId, closedBy);
            return ResponseEntity.ok(closedComplaint);
        } catch (RuntimeException e) {
            log.error("Error closing complaint: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error closing complaint: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to close complaint: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{complaintId}/replies")
    public ResponseEntity<?> addReply(
            @PathVariable Long complaintId,
            @RequestBody ComplaintReply reply) {
        try {
            log.info("Adding reply to complaint {}", complaintId);
            ComplaintReply savedReply = complaintService.addReply(complaintId, reply);
            return ResponseEntity.ok(savedReply);
        } catch (RuntimeException e) {
            log.error("Error adding reply: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding reply: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to add reply: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{complaintId}/replies")
    public ResponseEntity<?> getComplaintReplies(@PathVariable Long complaintId) {
        try {
            log.info("Fetching replies for complaint {}", complaintId);
            List<ComplaintReply> replies = complaintService.getComplaintReplies(complaintId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            log.error("Error fetching complaint replies: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch replies: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{complaintId}/replies/mark-read")
    public ResponseEntity<?> markRepliesAsRead(@PathVariable Long complaintId) {
        try {
            log.info("Marking replies as read for complaint {}", complaintId);
            complaintService.markRepliesAsRead(complaintId);
            return ResponseEntity.ok(Map.of("message", "Replies marked as read"));
        } catch (RuntimeException e) {
            log.error("Error marking replies as read: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error marking replies as read: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to mark replies as read: " + e.getMessage()));
        }
    }
}
