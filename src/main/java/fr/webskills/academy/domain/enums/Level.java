package fr.webskills.academy.domain.enums;

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
}
