package com.example.demo.controller;

import com.example.demo.dto.PanRequest;
import com.example.demo.dto.PanResponse;
import com.example.demo.dto.UserProfileRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@Valid @RequestBody UserProfileRequest request) {
        UserProfileResponse response = userService.getUserProfile(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pan")
    public ResponseEntity<PanResponse> getByPan(@Valid @RequestBody PanRequest request) {
        PanResponse response = userService.getUserDetailsByPan(request);
        return ResponseEntity.ok(response);
    }
}
