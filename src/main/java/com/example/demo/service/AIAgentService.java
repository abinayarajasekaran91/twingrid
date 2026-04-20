package com.example.demo.service;

import com.example.demo.dto.PortfolioRequest;
import com.example.demo.dto.PortfolioResponse;
import com.example.demo.entity.ClientPortfolio;
import com.example.demo.entity.PortfolioHolding;
import com.example.demo.entity.PortfolioRecommendation;
import com.example.demo.repository.ClientPortfolioRepository;
import com.example.demo.repository.PortfolioHoldingRepository;
import com.example.demo.repository.PortfolioRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AIAgentService {

    @Autowired
    private ClientPortfolioRepository portfolioRepo;

    @Autowired
    private PortfolioHoldingRepository holdingRepo;

    @Autowired
    private PortfolioRecommendationRepository recommendationRepo;

    public PortfolioResponse analyzePortfolio(PortfolioRequest request) {
        PortfolioResponse response = new PortfolioResponse();
        
        String clientId = request.getClientId() != null ? request.getClientId() : "U1001";
        Optional<ClientPortfolio> optionalPortfolio = portfolioRepo.findByClientId(clientId);

        if (optionalPortfolio.isEmpty()) {
            response.setStatus("ERROR");
            response.setMessage("Portfolio not found for client ID: " + clientId);
            return response;
        }

        ClientPortfolio portfolio = optionalPortfolio.get();
        response.setStatus("SUCCESS");
        response.setMessage("Portfolio analyzed successfully from database");
        response.setClientName(portfolio.getClientName());
        response.setRiskLevel(portfolio.getRiskLevel());
        response.setCurrentAllocations(Arrays.asList(portfolio.getCurrentEquity(), portfolio.getCurrentCommodity(), portfolio.getCurrentDebt()));
        response.setTargetAllocations(Arrays.asList(portfolio.getTargetEquity(), portfolio.getTargetCommodity(), portfolio.getTargetDebt()));
        response.setTotalRebalanceCost(portfolio.getTotalRebalanceCost());

        // Fetch Holdings (Matrix Data)
        List<PortfolioHolding> holdings = holdingRepo.findByClientPortfolioId(portfolio.getId());
        List<Map<String, Object>> matrix = new ArrayList<>();
        for (PortfolioHolding h : holdings) {
            Map<String, Object> row = new HashMap<>();
            row.put("fund", h.getFund().getName());
            row.put("m", h.getFund().getMeticulousOverlap());
            row.put("r", h.getFund().getRiskOverlap());
            row.put("mt", h.getFund().getMeatOverlap());
            row.put("iconColor", h.getFund().getIconColor());
            matrix.add(row);
        }
        response.setMatrix(matrix);

        // Fetch Recommendations
        List<PortfolioRecommendation> recs = recommendationRepo.findByClientPortfolioId(portfolio.getId());
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (PortfolioRecommendation r : recs) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", r.getStockName());
            row.put("reason", r.getReason());
            row.put("action", r.getAction());
            row.put("change", r.getChangePercentage());
            row.put("current", r.getCurrentValue());
            row.put("color", r.getIconColor());
            recommendations.add(row);
        }
        response.setRecommendations(recommendations);

        return response;
    }
}
