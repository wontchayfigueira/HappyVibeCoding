package com.happyvibe.model;

public record User(long id, String username, String passwordHash) {
    @Override
    public String toString() {
        return username;
    }
}
