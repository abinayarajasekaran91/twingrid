package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_portfolios")
public class ClientPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;
    private String clientName;
    private String riskLevel;
    
    private int currentEquity;
    private int currentCommodity;
    private int currentDebt;

    private int targetEquity;
    private int targetCommodity;
    private int targetDebt;

    private String totalRebalanceCost;
    private Double investedAmount;
    private Double currentValue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public int getCurrentEquity() { return currentEquity; }
    public void setCurrentEquity(int currentEquity) { this.currentEquity = currentEquity; }

    public int getCurrentCommodity() { return currentCommodity; }
    public void setCurrentCommodity(int currentCommodity) { this.currentCommodity = currentCommodity; }

    public int getCurrentDebt() { return currentDebt; }
    public void setCurrentDebt(int currentDebt) { this.currentDebt = currentDebt; }

    public int getTargetEquity() { return targetEquity; }
    public void setTargetEquity(int targetEquity) { this.targetEquity = targetEquity; }

    public int getTargetCommodity() { return targetCommodity; }
    public void setTargetCommodity(int targetCommodity) { this.targetCommodity = targetCommodity; }

    public int getTargetDebt() { return targetDebt; }
    public void setTargetDebt(int targetDebt) { this.targetDebt = targetDebt; }

    public String getTotalRebalanceCost() { return totalRebalanceCost; }
    public void setTotalRebalanceCost(String totalRebalanceCost) { this.totalRebalanceCost = totalRebalanceCost; }

    public Double getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(Double investedAmount) { this.investedAmount = investedAmount; }

    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }
}
