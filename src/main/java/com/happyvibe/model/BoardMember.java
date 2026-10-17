package com.happyvibe.model;

public record BoardMember(long userId, String username, String role) {
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
