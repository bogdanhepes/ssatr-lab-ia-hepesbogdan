package com.example.demo.messaging;

import com.example.demo.service.SessionService;
import com.example.demo.service.SessionService.SessionStats;
import com.example.demo.websocket.SessionUpdate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMessageListener {

    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public FeedbackMessageListener(SessionService sessionService, SimpMessagingTemplate messagingTemplate) {
        this.sessionService = sessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitConfig.FEEDBACK_QUEUE)
    public void handleFeedbackMessage(String message) {

        // mesajul e: "New feedback for session <TOKEN>"
        String token = extractToken(message);

        System.out.println("📩 RabbitMQ message received: " + message);

        if (token == null) {
            System.out.println("⚠️ Could not extract token from message, skipping websocket update.");
            return;
        }

        SessionStats stats = sessionService.getStats(token);

        SessionUpdate update = new SessionUpdate(
                token,
                stats.totalResponses,
                stats.positive,
                stats.neutral,
                stats.negative,
                stats.responseRatePercent
        );

        // trimitem update către dashboard (topic specific pe token)
        messagingTemplate.convertAndSend("/topic/session/" + token, update);
    }

    private String extractToken(String message) {
        String prefix = "New feedback for session ";
        if (message != null && message.startsWith(prefix)) {
            return message.substring(prefix.length()).trim();
        }
        return null;
    }
}
