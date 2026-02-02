package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feedback_responses")
public class FeedbackResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // legăm feedback-ul de o sesiune
    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    private Integer rating;

    @Column(length = 2000)
    private String comment;

    private Boolean anonymous;

    // dacă anonymous = false, participantul poate trimite un nume
    private String participantName;

    // răspunsuri la întrebări (simplu: text cu separare pe linii)
    @Column(length = 4000)
    private String answersText;

    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    public FeedbackResponse() { }

    public FeedbackResponse(Session session, Integer rating, String comment, Boolean anonymous,
                            String participantName, String answersText) {
        this.session = session;
        this.rating = rating;
        this.comment = comment;
        this.anonymous = anonymous;
        this.participantName = participantName;
        this.answersText = answersText;
        this.createdAt = Instant.now();
        this.sentiment = Sentiment.NEUTRAL;
    }

    public Long getId() { return id; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean anonymous) { this.anonymous = anonymous; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getAnswersText() { return answersText; }
    public void setAnswersText(String answersText) { this.answersText = answersText; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Sentiment getSentiment() { return sentiment; }
    public void setSentiment(Sentiment sentiment) { this.sentiment = sentiment; }
}
