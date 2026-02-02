package com.example.demo.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String FEEDBACK_QUEUE = "feedback.queue";

    @Bean
    public Queue feedbackQueue() {
        return new Queue(FEEDBACK_QUEUE, false);
    }
}
