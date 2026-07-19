package fr.webskills.academy.domain.enums;

import java.text.Normalizer;
import java.util.Locale;

public enum Level {
    BEGINNER("Débutant"),
    INTERMEDIATE("Intermédiaire"),
    ADVANCED("Avancé"),
    EXPERT("Expert");

    private final String label;

    Level(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Level fromQuery(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalize(value);
        for (Level level : values()) {
            if (normalize(level.name()).equals(normalized)
                    || normalize(level.label).equals(normalized)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Niveau inconnu: " + value);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }
}
