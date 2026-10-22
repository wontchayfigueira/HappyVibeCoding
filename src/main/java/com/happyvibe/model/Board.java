package com.happyvibe.model;

public record Board(long id, String name, long ownerId) {
    @Override
    public String toString() {
        return name;
    }
}
