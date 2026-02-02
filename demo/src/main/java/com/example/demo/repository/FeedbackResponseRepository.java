package com.example.demo.repository;

import com.example.demo.entity.FeedbackResponse;
import com.example.demo.entity.Sentiment;
import com.example.demo.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Long> {

    long countBySession(Session session);

    long countBySessionAndSentiment(Session session, Sentiment sentiment);

    List<FeedbackResponse> findBySession(Session session);
}
