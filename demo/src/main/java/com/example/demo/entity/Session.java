package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(unique = true, nullable = false)
    private String token;

    private Instant startTime;
    private Instant endTime;

    private Integer expectedParticipants;

    // Întrebări salvate simplu ca text cu separare pe linii (1 întrebare / linie)
    @Column(length = 4000)
    private String questionsText;

    // numele template-ului (dacă organizer a folosit template)
    private String templateName;

    public Session() { }

    public Session(String title, Instant startTime, Instant endTime, Integer expectedParticipants,
                   String questionsText, String templateName) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedParticipants = expectedParticipants;
        this.questionsText = questionsText;
        this.templateName = templateName;
        this.token = UUID.randomUUID().toString();
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getToken() { return token; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Integer getExpectedParticipants() { return expectedParticipants; }
    public void setExpectedParticipants(Integer expectedParticipants) { this.expectedParticipants = expectedParticipants; }

    public String getQuestionsText() { return questionsText; }
    public void setQuestionsText(String questionsText) { this.questionsText = questionsText; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
}
