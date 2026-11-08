package com.happyvibe.model;

public enum SortMode {
    DUE_DATE("Due date"),
    COURSE("Course"),
    TITLE("Title");

    private final String label;

    SortMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
