package com.example.demo.controller;

import com.example.demo.dto.PortfolioRequest;
import com.example.demo.dto.PortfolioResponse;
import com.example.demo.service.AIAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

