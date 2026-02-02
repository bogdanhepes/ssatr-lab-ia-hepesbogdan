package com.example.demo.websocket;

public class SessionUpdate {
    public String token;
    public long totalResponses;
    public long positive;
    public long neutral;
    public long negative;
    public double responseRatePercent;

    public SessionUpdate() {}

    public SessionUpdate(String token, long totalResponses, long positive, long neutral, long negative, double responseRatePercent) {
        this.token = token;
        this.totalResponses = totalResponses;
        this.positive = positive;
        this.neutral = neutral;
        this.negative = negative;
        this.responseRatePercent = responseRatePercent;
    }
}
