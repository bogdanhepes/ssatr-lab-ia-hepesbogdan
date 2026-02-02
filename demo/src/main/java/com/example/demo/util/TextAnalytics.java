package com.example.demo.util;

import java.util.*;
import java.util.regex.Pattern;

public class TextAnalytics {

    // stopwords minimale (română + câteva generale)
    private static final Set<String> STOPWORDS = Set.of(
            "si", "sau", "dar", "ca", "cu", "la", "in", "pe", "din", "de", "un", "o", "a", "ai", "am",
            "este", "sunt", "foarte", "mai", "mult", "putin", "nu", "da", "ok", "cam", "tot", "pentru",
            "the", "and", "or", "to", "of", "is", "are", "was", "were", "it", "this", "that"
    );

    private static final Pattern SPLIT = Pattern.compile("[^\\p{L}\\p{Nd}]+"); // separă pe non-lit/număr

    public static List<WordCount> topWords(List<String> texts, int limit) {
        Map<String, Integer> freq = new HashMap<>();

        for (String t : texts) {
            if (t == null) continue;
            String lower = t.toLowerCase(Locale.ROOT);

            for (String raw : SPLIT.split(lower)) {
                String w = raw.trim();
                if (w.isEmpty()) continue;
                if (w.length() < 3) continue;
                if (STOPWORDS.contains(w)) continue;

                freq.put(w, freq.getOrDefault(w, 0) + 1);
            }
        }

        List<WordCount> list = new ArrayList<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            list.add(new WordCount(e.getKey(), e.getValue()));
        }

        list.sort((a, b) -> Integer.compare(b.count, a.count));
        if (list.size() > limit) return list.subList(0, limit);
        return list;
    }

    public static class WordCount {
        public String word;
        public int count;

        public WordCount() { }

        public WordCount(String word, int count) {
            this.word = word;
            this.count = count;
        }
    }
}
