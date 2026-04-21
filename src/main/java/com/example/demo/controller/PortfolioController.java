package com.example.demo.controller;

import com.example.demo.dto.PortfolioRequest;
import com.example.demo.dto.PortfolioResponse;
import com.example.demo.dto.ShareEmailRequest;
import com.example.demo.service.AIAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private AIAgentService aiAgentService;

    @PostMapping("/analyze")
    public PortfolioResponse analyzePortfolio(@RequestBody PortfolioRequest request) {
        return aiAgentService.analyzePortfolio(request);
    }

    @PostMapping("/overlap-validation")
    public java.util.Map<String, Object> validateOverlap(@RequestBody PortfolioRequest request) {
        return aiAgentService.validateOverlap(request.getPan());
    }

    @PostMapping("/share-email")
    public java.util.Map<String, String> shareEmail(@RequestBody ShareEmailRequest request) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        try {
            aiAgentService.sharePortfolioByEmail(request.getClientId(), request.getRecipientEmail(),
                    request.getLanguage(), request.getMode(), request.getPan());
            response.put("status", "SUCCESS");
            response.put("message", "Email shared successfully with " + request.getRecipientEmail());
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to share email: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}

