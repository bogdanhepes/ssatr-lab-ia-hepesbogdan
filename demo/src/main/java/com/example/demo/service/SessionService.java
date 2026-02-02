package com.example.demo.service;

import com.example.demo.entity.FeedbackResponse;
import com.example.demo.entity.Sentiment;
import com.example.demo.entity.Session;
import com.example.demo.messaging.RabbitConfig;
import com.example.demo.repository.FeedbackResponseRepository;
import com.example.demo.repository.SessionRepository;
import com.example.demo.util.TemplateCatalog;
import com.example.demo.util.TextAnalytics;
import com.example.demo.util.TextAnalytics.WordCount;
import com.example.demo.util.SentimentAnalyzer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final FeedbackResponseRepository feedbackResponseRepository;
    private final RabbitTemplate rabbitTemplate;

    public SessionService(SessionRepository sessionRepository,
                          FeedbackResponseRepository feedbackResponseRepository,
                          RabbitTemplate rabbitTemplate) {
        this.sessionRepository = sessionRepository;
        this.feedbackResponseRepository = feedbackResponseRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Organizer: create session cu întrebări custom sau template
    public Session createSession(String title,
                                 Instant startTime,
                                 Instant endTime,
                                 Integer expectedParticipants,
                                 List<String> questions,
                                 String templateName) {

        List<String> finalQuestions = new ArrayList<>();

        if (questions != null && !questions.isEmpty()) {
            for (String q : questions) {
                if (q != null && !q.trim().isEmpty()) {
                    finalQuestions.add(q.trim());
                }
            }
        }

        if (finalQuestions.isEmpty()) {
            finalQuestions = TemplateCatalog.getTemplateQuestions(templateName);
        }

        String questionsText = String.join("\n", finalQuestions);

        Session session = new Session(title, startTime, endTime, expectedParticipants, questionsText, templateName);
        return sessionRepository.save(session);
    }

    // Participant: submit feedback (cu window enforcement + nume dacă nu e anonim + answers)
    public FeedbackResponse submitFeedback(String token,
                                           Integer rating,
                                           String comment,
                                           Boolean anonymous,
                                           String participantName,
                                           List<String> answers) {

        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found for token: " + token));

        // feedback window enforcement
        Instant now = Instant.now();
        if (session.getStartTime() != null && now.isBefore(session.getStartTime())) {
            throw new RuntimeException("Feedback window not started yet.");
        }
        if (session.getEndTime() != null && now.isAfter(session.getEndTime())) {
            throw new RuntimeException("Feedback window is closed.");
        }

        // nume dacă NU e anonim
        boolean isAnonymous = anonymous != null && anonymous;
        String nameToSave = null;
        if (!isAnonymous) {
            if (participantName == null || participantName.trim().isEmpty()) {
                throw new RuntimeException("Name is required when anonymous = false.");
            }
            nameToSave = participantName.trim();
        }

        // answers -> text cu separare pe linii
        String answersText = "";
        if (answers != null && !answers.isEmpty()) {
            List<String> clean = new ArrayList<>();
            for (String a : answers) clean.add(a == null ? "" : a.trim());
            answersText = String.join("\n", clean);
        }

        FeedbackResponse response = new FeedbackResponse(
                session,
                rating,
                comment,
                isAnonymous,
                nameToSave,
                answersText
        );

        // ✅ sentiment combinat (text + rating)
        Sentiment sentiment = SentimentAnalyzer.analyze(comment, rating);
        response.setSentiment(sentiment);

        FeedbackResponse saved = feedbackResponseRepository.save(response);

        // RabbitMQ event (pentru dashboard live)
        rabbitTemplate.convertAndSend(
                RabbitConfig.FEEDBACK_QUEUE,
                "New feedback for session " + session.getToken()
        );

        return saved;
    }

    // pentru UI: detalii sesiune (întrebări + dacă e în fereastră)
    public SessionDetails getSessionDetails(String token) {
        Session s = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found for token: " + token));

        Instant now = Instant.now();
        boolean started = (s.getStartTime() == null) || !now.isBefore(s.getStartTime());
        boolean ended = (s.getEndTime() != null) && now.isAfter(s.getEndTime());
        boolean active = started && !ended;

        List<String> questions = new ArrayList<>();
        if (s.getQuestionsText() != null && !s.getQuestionsText().isBlank()) {
            for (String line : s.getQuestionsText().split("\\R")) {
                if (!line.trim().isEmpty()) questions.add(line.trim());
            }
        }

        return new SessionDetails(s.getTitle(), s.getToken(), s.getStartTime(), s.getEndTime(), active, questions);
    }

    public static class SessionDetails {
        public String title;
        public String token;
        public Instant startTime;
        public Instant endTime;
        public boolean active;
        public List<String> questions;

        public SessionDetails(String title, String token, Instant startTime, Instant endTime, boolean active, List<String> questions) {
            this.title = title;
            this.token = token;
            this.startTime = startTime;
            this.endTime = endTime;
            this.active = active;
            this.questions = questions;
        }
    }

    // Stats + top words + participants
    public SessionStats getStats(String token) {
        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found for token: " + token));

        List<FeedbackResponse> all = feedbackResponseRepository.findBySession(session);

        long total = all.size();
        long positive = all.stream().filter(r -> r.getSentiment() == Sentiment.POSITIVE).count();
        long neutral  = all.stream().filter(r -> r.getSentiment() == Sentiment.NEUTRAL).count();
        long negative = all.stream().filter(r -> r.getSentiment() == Sentiment.NEGATIVE).count();

        int expected = session.getExpectedParticipants() == null ? 0 : session.getExpectedParticipants();
        double responseRate = expected > 0 ? (total * 100.0 / expected) : 0.0;

        // top words din comment + answersText
        List<String> texts = new ArrayList<>();
        for (FeedbackResponse r : all) {
            if (r.getComment() != null) texts.add(r.getComment());
            if (r.getAnswersText() != null) texts.add(r.getAnswersText());
        }
        List<WordCount> topWords = TextAnalytics.topWords(texts, 10);

        // ✅ Participants: nume unice + număr anonimi
        Set<String> uniqueNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        long anonymousCount = 0;

        for (FeedbackResponse r : all) {
            boolean anon = r.getAnonymous() != null && r.getAnonymous();
            if (anon) {
                anonymousCount++;
            } else {
                if (r.getParticipantName() != null && !r.getParticipantName().trim().isEmpty()) {
                    uniqueNames.add(r.getParticipantName().trim());
                }
            }
        }

        List<String> namedParticipants = new ArrayList<>(uniqueNames);
        namedParticipants.sort(String.CASE_INSENSITIVE_ORDER);

        return new SessionStats(total, positive, neutral, negative, expected, responseRate, topWords, namedParticipants, anonymousCount);
    }

    public static class SessionStats {
        public long totalResponses;
        public long positive;
        public long neutral;
        public long negative;
        public int expectedParticipants;
        public double responseRatePercent;
        public List<WordCount> topWords;

        // ✅ new
        public List<String> namedParticipants;
        public long anonymousCount;

        public SessionStats(long totalResponses, long positive, long neutral, long negative,
                            int expectedParticipants, double responseRatePercent, List<WordCount> topWords,
                            List<String> namedParticipants, long anonymousCount) {
            this.totalResponses = totalResponses;
            this.positive = positive;
            this.neutral = neutral;
            this.negative = negative;
            this.expectedParticipants = expectedParticipants;
            this.responseRatePercent = responseRatePercent;
            this.topWords = topWords;
            this.namedParticipants = namedParticipants;
            this.anonymousCount = anonymousCount;
        }
    }
}
