package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "portfolio_holdings")
public class PortfolioHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_portfolio_id")
    private ClientPortfolio clientPortfolio;

    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ClientPortfolio getClientPortfolio() { return clientPortfolio; }
    public void setClientPortfolio(ClientPortfolio clientPortfolio) { this.clientPortfolio = clientPortfolio; }

    public Fund getFund() { return fund; }
    public void setFund(Fund fund) { this.fund = fund; }
}
