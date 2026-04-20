package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_holdings")
public class StockHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    private String stockName;
    private double weight; // Percentage e.g. 8.5 for 8.5%
    private String sector; // e.g. BFSI, IT, Healthcare
    private String marketCap; // e.g. Large, Mid, Small

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Fund getFund() { return fund; }
    public void setFund(Fund fund) { this.fund = fund; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getMarketCap() { return marketCap; }
    public void setMarketCap(String marketCap) { this.marketCap = marketCap; }
}
