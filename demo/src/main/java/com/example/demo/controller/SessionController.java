package com.example.demo.controller;

import com.example.demo.entity.Session;
import com.example.demo.service.SessionService;
import com.example.demo.service.SessionService.SessionDetails;
import com.example.demo.service.SessionService.SessionStats;
import com.example.demo.util.QrCodeUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // Organizer: create session cu template/questions
    @PostMapping
    public CreateSessionResponse createSession(@RequestBody CreateSessionRequest request) {
        Session session = sessionService.createSession(
                request.title,
                request.startTime,
                request.endTime,
                request.expectedParticipants,
                request.questions,
                request.templateName
        );

        String participantLink = "http://localhost:8081/feedback.html?token=" + session.getToken();
        String dashboardLink = "http://localhost:8081/dashboard.html?token=" + session.getToken();
        String qrImageLink = "http://localhost:8081/api/sessions/" + session.getToken() + "/qr";

        return new CreateSessionResponse(session, participantLink, dashboardLink, qrImageLink);
    }

    // Participant: submit feedback (include answers + name when not anonymous)
    @PostMapping("/{token}/responses")
    public String submitFeedback(@PathVariable String token, @RequestBody SubmitFeedbackRequest request) {
        sessionService.submitFeedback(
                token,
                request.rating,
                request.comment,
                request.anonymous,
                request.participantName,
                request.answers
        );
        return "Submitted";
    }

    // Dashboard: stats (now includes topWords)
    @GetMapping("/{token}/stats")
    public SessionStats stats(@PathVariable String token) {
        return sessionService.getStats(token);
    }

    // UI needs session info (questions + active)
    @GetMapping("/{token}")
    public SessionDetails sessionDetails(@PathVariable String token) {
        return sessionService.getSessionDetails(token);
    }

    // Organizer: QR code PNG
    @GetMapping(value = "/{token}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQr(@PathVariable String token) {
        String participantLink = "http://localhost:8081/feedback.html?token=" + token;
        byte[] png = QrCodeUtil.generatePng(participantLink, 300, 300);
        return ResponseEntity.ok(png);
    }

    // ----- DTOs -----

    public static class CreateSessionRequest {
        public String title;
        public Instant startTime;
        public Instant endTime;
        public Integer expectedParticipants;

        // new:
        public List<String> questions;     // custom questions
        public String templateName;        // ex: "event", "course", "product"
    }

    public static class SubmitFeedbackRequest {
        public Integer rating;
        public String comment;
        public Boolean anonymous;

        // new:
        public String participantName;     // required if anonymous=false
        public List<String> answers;       // answers for questions, same order
    }

    public static class CreateSessionResponse {
        public Session session;
        public String participantLink;
        public String dashboardLink;
        public String qrImageLink;

        public CreateSessionResponse(Session session, String participantLink, String dashboardLink, String qrImageLink) {
            this.session = session;
            this.participantLink = participantLink;
            this.dashboardLink = dashboardLink;
            this.qrImageLink = qrImageLink;
        }
    }
}
