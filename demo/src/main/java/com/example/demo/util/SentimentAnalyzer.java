package com.example.demo.util;

import com.example.demo.entity.Sentiment;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class SentimentAnalyzer {

    private static final List<String> POSITIVE_WORDS = List.of(
            "bun", "excelent", "super", "tare", "fain", "minunat", "frumos", "placut", "perfect",
            "recomand", "mi-a placut", "mi a placut", "ok"
    );

    private static final List<String> NEGATIVE_WORDS = List.of(
            "prost", "slab", "plictisitor", "naspa", "rau", "oribil", "dezamagitor",
            "nu mi-a placut", "nu mi a placut"
    );

    // normalizează textul: lowercase + fără diacritice (plăcut -> placut)
    private static String normalize(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase(Locale.ROOT);
        String norm = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}", ""); // scoate diacritice
    }

    // Sentiment din text (doar cuvinte)
    public static Sentiment analyzeText(String text) {
        String lower = normalize(text).trim();
        if (lower.isEmpty()) return Sentiment.NEUTRAL;

        int pos = 0;
        int neg = 0;

        for (String w : POSITIVE_WORDS) {
            if (lower.contains(w)) pos++;
        }
        for (String w : NEGATIVE_WORDS) {
            if (lower.contains(w)) neg++;
        }

        if (pos > neg) return Sentiment.POSITIVE;
        if (neg > pos) return Sentiment.NEGATIVE;
        return Sentiment.NEUTRAL;
    }

    // Sentiment combinat: text + rating
    // - dacă textul e clar (pozitiv/negativ), îl folosim
    // - dacă textul e NEUTRAL, folosim rating:
    //   rating >=4 -> POSITIVE, rating <=2 -> NEGATIVE, rating==3 -> NEUTRAL
    public static Sentiment analyze(String comment, Integer rating) {
        Sentiment byText = analyzeText(comment);
        if (byText != Sentiment.NEUTRAL) return byText;

        if (rating == null) return Sentiment.NEUTRAL;
        if (rating >= 4) return Sentiment.POSITIVE;
        if (rating <= 2) return Sentiment.NEGATIVE;
        return Sentiment.NEUTRAL;
    }
}
