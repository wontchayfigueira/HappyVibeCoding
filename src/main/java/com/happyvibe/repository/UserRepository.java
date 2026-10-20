package com.happyvibe.repository;

import com.happyvibe.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long create(String username, String passwordHash) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users(username, password_hash) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            return ps;
        }, key);
        if (key.getKey() == null) {
            throw new IllegalStateException("Could not create user");
        }
        return key.getKey().longValue();
    }

    public Optional<User> findByUsername(String username) {
        List<User> result = jdbc.query(
                "SELECT id, username, password_hash FROM users WHERE lower(username) = lower(?)",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash")),
                username);
        return result.stream().findFirst();
    }

    public Optional<User> findById(long id) {
        List<User> result = jdbc.query(
                "SELECT id, username, password_hash FROM users WHERE id = ?",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash")),
                id);
        return result.stream().findFirst();
    }

    public int count() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return count == null ? 0 : count;
    }
}
