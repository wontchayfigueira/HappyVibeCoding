package com.happyvibe.service;

import com.happyvibe.model.User;
import com.happyvibe.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("User already exists.");
        }

        // generates a safety hash for each password
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(hashedPassword);

        return userRepository.save(newUser);
    }

    public User login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // password vs saved hash
            if (BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
                AppSession.getInstance().setCurrentUser(user);
                return user;
            }
        }
        throw new IllegalArgumentException("Invalid Credentials.");
    }
}