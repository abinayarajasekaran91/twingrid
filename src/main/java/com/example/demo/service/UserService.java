package com.example.demo.service;

import com.example.demo.dto.PanRequest;
import com.example.demo.dto.PanResponse;
import com.example.demo.dto.UserProfileRequest;
import com.example.demo.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(UserProfileRequest request);
    PanResponse getUserDetailsByPan(PanRequest request);
}
