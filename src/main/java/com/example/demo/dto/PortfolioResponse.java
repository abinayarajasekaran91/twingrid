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
}
