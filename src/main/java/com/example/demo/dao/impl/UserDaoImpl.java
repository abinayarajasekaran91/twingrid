package com.example.demo.dao.impl;

import com.example.demo.dao.UserDao;
import com.example.demo.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        return user;
    };

    private final RowMapper<User> userProfileRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setAge(rs.getObject("age", Integer.class));
        user.setCity(rs.getString("city"));
        user.setAddress(rs.getString("address"));
        java.sql.Date dob = rs.getDate("dob");
        if (dob != null) {
            user.setDob(dob.toLocalDate());
        }
        return user;
    };

    @Override
    public Optional<User> findByEmail(String email) {
        System.out.println("DB hit starting...");
        String sql = "SELECT id, email, password FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            System.out.println("User Found : " + user);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findProfileByEmail(String email) {
        String sql = "SELECT id, email, password, name, age, dob, city, address FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userProfileRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByPan(String pan) {
        String sql = "SELECT id, email, password, name, age, dob, city, address, pan FROM users WHERE pan = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setName(rs.getString("name"));
                u.setAge(rs.getObject("age", Integer.class));
                u.setCity(rs.getString("city"));
                u.setAddress(rs.getString("address"));
                u.setPan(rs.getString("pan"));
                java.sql.Date dob = rs.getDate("dob");
                if (dob != null) {
                    u.setDob(dob.toLocalDate());
                }
                return u;
            }, pan);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
