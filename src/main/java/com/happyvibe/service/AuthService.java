package com.happyvibe.service;

import com.happyvibe.model.User;
import com.happyvibe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AppSession session;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, AppSession session) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.session = session;
    }

    public User register(String username, String password) {
        String cleanUsername = cleanUsername(username);
        validatePassword(password);
        if (users.findByUsername(cleanUsername).isPresent()) {
            throw new IllegalArgumentException("That username already exists.");
        }
        long id = users.create(cleanUsername, passwordEncoder.encode(password));
        User user = users.findById(id).orElseThrow();
        session.login(user);
        return user;
    }

    public User login(String username, String password) {
        String cleanUsername = cleanUsername(username);
        User user = users.findByUsername(cleanUsername)
                .orElseThrow(() -> new IllegalArgumentException("Wrong username or password."));
        if (!passwordEncoder.matches(password == null ? "" : password, user.passwordHash())) {
            throw new IllegalArgumentException("Wrong username or password.");
        }
        session.login(user);
        return user;
    }

    public void logout() {
        session.logout();
    }

    private String cleanUsername(String username) {
        if (username == null || username.trim().length() < 3 || username.trim().length() > 30) {
            throw new IllegalArgumentException("Username must be 3 to 30 characters.");
        }
        if (!username.trim().matches("[A-Za-z0-9_.-]+")) {
            throw new IllegalArgumentException("Username may use letters, numbers, dot, dash and underscore only.");
        }
        return username.trim();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must have at least 6 characters.");
        }
    }
}
