package com.example.demo.controller;

import com.example.demo.entity.Session;
import com.example.demo.repository.SessionRepository;
import com.example.demo.service.SessionService;
import com.example.demo.service.SessionService.SessionStats;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final SessionService sessionService;
    private final SessionRepository sessionRepository;

    public AnalyticsController(SessionService sessionService, SessionRepository sessionRepository) {
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
    }

    // ✅ NOU: overview pentru TOATE sesiunile existente
    // Returnează lista de sesiuni + stats pentru fiecare.
    @GetMapping("/sessions")
    public List<SessionOverviewItem> sessionsOverview() {

        List<Session> sessions = sessionRepository.findAll();
        List<SessionOverviewItem> result = new ArrayList<>();

        for (Session s : sessions) {
            SessionStats stats = sessionService.getStats(s.getToken());
            result.add(new SessionOverviewItem(
                    s.getId(),
                    s.getTitle(),
                    s.getToken(),
                    stats.totalResponses,
                    stats.positive,
                    stats.neutral,
                    stats.negative,
                    stats.expectedParticipants,
                    stats.responseRatePercent,
                    stats.namedParticipants,
                    stats.anonymousCount
            ));
        }

        // sortăm descrescător după total responses (ca să fie “vizual” mai ok)
        result.sort(Comparator.comparingLong((SessionOverviewItem x) -> x.totalResponses).reversed());

        return result;
    }

    // ----- DTOs -----

    public static class SessionOverviewItem {
        public Long id;
        public String title;
        public String token;

        public long totalResponses;
        public long positive;
        public long neutral;
        public long negative;

        public int expectedParticipants;
        public double responseRatePercent;

        public List<String> namedParticipants;
        public long anonymousCount;

        public SessionOverviewItem(Long id, String title, String token,
                                   long totalResponses, long positive, long neutral, long negative,
                                   int expectedParticipants, double responseRatePercent,
                                   List<String> namedParticipants, long anonymousCount) {
            this.id = id;
            this.title = title;
            this.token = token;
            this.totalResponses = totalResponses;
            this.positive = positive;
            this.neutral = neutral;
            this.negative = negative;
            this.expectedParticipants = expectedParticipants;
            this.responseRatePercent = responseRatePercent;
            this.namedParticipants = namedParticipants;
            this.anonymousCount = anonymousCount;
        }
    }
}
