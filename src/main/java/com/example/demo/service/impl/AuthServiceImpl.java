package com.example.demo.service.impl;

import com.example.demo.dao.UserDao;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotRegisteredException;
import com.example.demo.service.AuthService;
import com.example.demo.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserDao userDao, JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse signin(LoginRequest loginRequest) {
        Optional<User> userOptional = userDao.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("Not registered user");
        }

        User user = userOptional.get();

        // Basic password matching (In real apps use BCryptPasswordEncoder)
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new UserNotRegisteredException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token);
    }
}
