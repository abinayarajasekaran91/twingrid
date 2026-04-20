package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class PortfolioResponse {
    private String status;
    private String message;
    private String clientName;
    private String riskLevel;
    private List<Integer> currentAllocations;
    private List<Integer> targetAllocations;
    private String totalRebalanceCost;
    private List<Map<String, Object>> recommendations;
    private List<Map<String, Object>> matrix;
    private List<Map<String, Object>> stockOverlap;
    private Map<String, Double> sectorOverlap;
    private Map<String, Double> marketCapOverlap;
    private List<Map<String, Object>> marketCapMatrix;
    private List<String> flags;
    private List<Map<String, String>> agentTrace; 
    
    // NEW: Production-grade rebalancing metrics
    private Map<String, Object> portfolioScores; // { "before": {...}, "after": {...} }
    private List<Map<String, Object>> optimizedActions; // Detailed actions with reasoning
    
    // Performance Metrics
    private Double currentValue;
    private Double investedAmount;
    private Double gain;
    private Double gainPercent;


    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<Integer> getCurrentAllocations() { return currentAllocations; }
    public void setCurrentAllocations(List<Integer> currentAllocations) { this.currentAllocations = currentAllocations; }

    public List<Integer> getTargetAllocations() { return targetAllocations; }
    public void setTargetAllocations(List<Integer> targetAllocations) { this.targetAllocations = targetAllocations; }

    public String getTotalRebalanceCost() { return totalRebalanceCost; }
    public void setTotalRebalanceCost(String totalRebalanceCost) { this.totalRebalanceCost = totalRebalanceCost; }

    public List<Map<String, Object>> getRecommendations() { return recommendations; }
    public void setRecommendations(List<Map<String, Object>> recommendations) { this.recommendations = recommendations; }

    public List<Map<String, Object>> getMatrix() { return matrix; }
    public void setMatrix(List<Map<String, Object>> matrix) { this.matrix = matrix; }

    public List<Map<String, Object>> getStockOverlap() { return stockOverlap; }
    public void setStockOverlap(List<Map<String, Object>> stockOverlap) { this.stockOverlap = stockOverlap; }

    public Map<String, Double> getSectorOverlap() { return sectorOverlap; }
    public void setSectorOverlap(Map<String, Double> sectorOverlap) { this.sectorOverlap = sectorOverlap; }

    public Map<String, Double> getMarketCapOverlap() { return marketCapOverlap; }
    public void setMarketCapOverlap(Map<String, Double> marketCapOverlap) { this.marketCapOverlap = marketCapOverlap; }

    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }

    public List<Map<String, Object>> getMarketCapMatrix() { return marketCapMatrix; }
    public void setMarketCapMatrix(List<Map<String, Object>> marketCapMatrix) { this.marketCapMatrix = marketCapMatrix; }

    public List<Map<String, String>> getAgentTrace() { return agentTrace; }
    public void setAgentTrace(List<Map<String, String>> agentTrace) { this.agentTrace = agentTrace; }

    public Map<String, Object> getPortfolioScores() { return portfolioScores; }
    public void setPortfolioScores(Map<String, Object> portfolioScores) { this.portfolioScores = portfolioScores; }

    public List<Map<String, Object>> getOptimizedActions() { return optimizedActions; }
    public void setOptimizedActions(List<Map<String, Object>> optimizedActions) { this.optimizedActions = optimizedActions; }

    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }

    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }

    public Double getGain() { return gain; }
    public void setGain(Double gain) { this.gain = gain; }

    public Double getGainPercent() { return gainPercent; }
    public void setGainPercent(Double gainPercent) { this.gainPercent = gainPercent; }
}

