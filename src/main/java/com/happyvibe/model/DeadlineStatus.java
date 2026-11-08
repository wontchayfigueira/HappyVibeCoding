package com.happyvibe.model;

public enum DeadlineStatus {
    OVERDUE("Overdue"),
    DUE_SOON("Due within 48 hours"),
    ON_TIME("On schedule"),
    NO_DEADLINE("No deadline"),
    COMPLETED("Completed");

    private final String label;

    DeadlineStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
