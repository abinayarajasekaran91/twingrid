package com.example.demo.service.impl;

import com.example.demo.dao.UserDao;
import com.example.demo.dto.PanRequest;
import com.example.demo.dto.PanResponse;
import com.example.demo.dto.UserProfileRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotRegisteredException;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserProfileResponse getUserProfile(UserProfileRequest request) {
        Optional<User> userOptional = userDao.findProfileByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("User not registered");
        }

        User user = userOptional.get();

        if (!user.getPassword().equals(request.getPassword())) {
            throw new UserNotRegisteredException("Invalid credentials");
        }

        return new UserProfileResponse(
                user.getEmail(),
                user.getName(),
                user.getAge(),
                user.getDob(),
                user.getCity(),
                user.getAddress()
        );
    }

    @Override
    public PanResponse getUserDetailsByPan(PanRequest request) {
        Optional<User> userOptional = userDao.findByPan(request.getPan());

        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("User not found for the given PAN");
        }

        User user = userOptional.get();

        return new PanResponse(
                user.getPan(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getDob(),
                user.getCity(),
                user.getAddress()
        );
    }
}

