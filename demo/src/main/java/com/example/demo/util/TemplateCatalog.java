package com.example.demo.util;

import java.util.List;

public class TemplateCatalog {

    public static List<String> getTemplateQuestions(String templateName) {
        if (templateName == null) return List.of();

        return switch (templateName.toLowerCase()) {
            case "event" -> List.of(
                    "Ce ți-a plăcut cel mai mult?",
                    "Ce ai îmbunătăți?",
                    "Cum ți s-a părut organizarea?"
            );
            case "course" -> List.of(
                    "Ce parte a fost cea mai clară?",
                    "Unde ai avut dificultăți?",
                    "Ce ai schimba la laborator / seminar?"
            );
            case "product" -> List.of(
                    "Ce ți-a plăcut la produs?",
                    "Ce nu ți-a plăcut?",
                    "L-ai recomanda? De ce?"
            );
            default -> List.of(
                    "Ce ți-a plăcut?",
                    "Ce ai îmbunătăți?"
            );
        };
    }
}
