package com.happyvibe.service;

import com.happyvibe.model.User;
import org.springframework.stereotype.Component;

@Component
public class AppSession {
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User currentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Please log in first.");
        }
        return currentUser;
    }
}
