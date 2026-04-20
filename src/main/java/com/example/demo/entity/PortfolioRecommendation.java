package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "portfolio_recommendations")
public class PortfolioRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_portfolio_id")
    private ClientPortfolio clientPortfolio;

    private String stockName;
    private String reason;
    private String action; // add, remove, none
    private String changePercentage;
    private String currentValue;
    private String iconColor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ClientPortfolio getClientPortfolio() { return clientPortfolio; }
    public void setClientPortfolio(ClientPortfolio clientPortfolio) { this.clientPortfolio = clientPortfolio; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getChangePercentage() { return changePercentage; }
    public void setChangePercentage(String changePercentage) { this.changePercentage = changePercentage; }

    public String getCurrentValue() { return currentValue; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }

    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }
}
