package com.example.demo.dao;

import com.example.demo.entity.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByEmail(String email);
}
