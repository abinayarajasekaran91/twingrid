package com.example.demo.config;

import com.example.demo.entity.ClientPortfolio;
import com.example.demo.entity.Fund;
import com.example.demo.entity.PortfolioHolding;
import com.example.demo.entity.PortfolioRecommendation;
import com.example.demo.repository.ClientPortfolioRepository;
import com.example.demo.repository.FundRepository;
import com.example.demo.repository.PortfolioHoldingRepository;
import com.example.demo.repository.PortfolioRecommendationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(FundRepository fundRepo,
                                   ClientPortfolioRepository portfolioRepo,
                                   PortfolioHoldingRepository holdingRepo,
                                   PortfolioRecommendationRepository recommendationRepo) {
        return args -> {
            // Check if data already exists
            if (fundRepo.count() > 0) {
                return;
            }

            // 1. Seed Real-World Mutual Funds (Inspired by top funds in India)
            Fund f1 = createFund("Parag Parikh Flexi Cap Fund", "Equity", 86, 42, 28, "#3b82f6");
            Fund f2 = createFund("Nippon India Small Cap Fund", "Equity", 42, 28, 17, "#f59e0b");
            Fund f3 = createFund("HDFC Mid-Cap Opportunities", "Equity", 17, 25, 8, "#8b5cf6");
            Fund f4 = createFund("SBI Bluechip Fund", "Equity", 25, 25, 8, "#0ea5e9");
            Fund f5 = createFund("ICICI Pru Bond Fund", "Debt", 80, 10, 10, "#10b981");
            Fund f6 = createFund("Aditya Birla Sun Life Digital", "Equity", 20, 90, 50, "#ef4444");
            Fund f7 = createFund("Kotak Emerging Equity", "Equity", 30, 85, 40, "#8b5cf6");
            Fund f8 = createFund("Axis Long Term Equity", "Equity", 15, 5, 5, "#3b82f6");
            Fund f9 = createFund("Mirae Asset Large Cap", "Equity", 60, 20, 10, "#10b981");
            Fund f10 = createFund("Tata Digital India Fund", "Equity", 95, 95, 80, "#8b5cf6");
            
            fundRepo.saveAll(Arrays.asList(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10));

            // 2. Seed Client Portfolios
            ClientPortfolio p1 = createPortfolio("U1001", "John Smith", "Moderate Risk", 85, 10, 5, 70, 30, 0, "-$72,135.64");
            ClientPortfolio p2 = createPortfolio("U1002", "Emma Davis", "High Risk", 40, 20, 40, 60, 20, 20, "-$15,420.00");
            ClientPortfolio p3 = createPortfolio("U1003", "Michael Chen", "Low Risk", 20, 0, 80, 40, 10, 50, "+$4,500.50");
            ClientPortfolio p4 = createPortfolio("U1004", "Sophia Patel", "Aggressive", 95, 5, 0, 80, 15, 5, "-$112,000.00");
            ClientPortfolio p5 = createPortfolio("U1005", "Liam Wilson", "Balanced", 60, 20, 20, 60, 20, 20, "$0.00");

            portfolioRepo.saveAll(Arrays.asList(p1, p2, p3, p4, p5));

            // 3. Seed Holdings (Mapping Portfolios to Funds)
            holdingRepo.saveAll(Arrays.asList(
                // John Smith holds f1, f2, f3, f4
                createHolding(p1, f1), createHolding(p1, f2), createHolding(p1, f3), createHolding(p1, f4),
                // Emma holds f5, f6, f7
                createHolding(p2, f5), createHolding(p2, f6), createHolding(p2, f7),
                // Michael holds f8, f9
                createHolding(p3, f8), createHolding(p3, f9),
                // Sophia holds f10, f6, f2
                createHolding(p4, f10), createHolding(p4, f6), createHolding(p4, f2),
                // Liam holds f4, f8
                createHolding(p5, f4), createHolding(p5, f8)
            ));

            // 4. Seed Recommendations
            recommendationRepo.saveAll(Arrays.asList(
                // John Smith
                createRec(p1, "Reliance Industries", "Increase large cap exposure", "add", "+6.20%", "$175,303", "#f97316"),
                createRec(p1, "TCS", "Concentration in IT sector", "remove", "+6.15%", "$2,111.16", "#1f2937"),
                createRec(p1, "Sun Pharma", "Healthcare underweight", "add", "+11.5%", "$1,234.40", "#1e3a8a"),
                createRec(p1, "SpiceJet", "Overweight in airlines sector", "remove", "-1.8%", "$1,545.40", "#eab308"),
                
                // Emma Davis
                createRec(p2, "Infosys", "Capitalizing on tech growth", "add", "+12.4%", "$15,000", "#10b981"),
                createRec(p2, "Govt Bonds", "Reduce debt exposure", "remove", "-0.5%", "$45,000", "#6b7280"),

                // Michael Chen
                createRec(p3, "Gold ETF", "Inflation hedge", "add", "+2.1%", "$5,000", "#f59e0b"),
                createRec(p3, "HDFC Bank", "Stable banking growth", "add", "+4.5%", "$12,000", "#1f2937"),

                // Sophia Patel
                createRec(p4, "Zomato", "High volatility risk", "remove", "-15.0%", "$25,000", "#f59e0b"),
                createRec(p4, "Silver ETF", "Diversify commodities", "add", "+1.2%", "$20,000", "#64748b"),

                // Liam Wilson
                createRec(p5, "No Action", "Portfolio is perfectly aligned", "none", "0.0%", "$0", "#6b7280")
            ));
        };
    }

    private Fund createFund(String name, String category, int m, int r, int mt, String color) {
        Fund f = new Fund();
        f.setName(name);
        f.setCategory(category);
        f.setMeticulousOverlap(m);
        f.setRiskOverlap(r);
        f.setMeatOverlap(mt);
        f.setIconColor(color);
        return f;
    }

    private ClientPortfolio createPortfolio(String cid, String name, String risk, int ce, int cc, int cd, int te, int tc, int td, String cost) {
        ClientPortfolio p = new ClientPortfolio();
        p.setClientId(cid);
        p.setClientName(name);
        p.setRiskLevel(risk);
        p.setCurrentEquity(ce);
        p.setCurrentCommodity(cc);
        p.setCurrentDebt(cd);
        p.setTargetEquity(te);
        p.setTargetCommodity(tc);
        p.setTargetDebt(td);
        p.setTotalRebalanceCost(cost);
        return p;
    }

    private PortfolioHolding createHolding(ClientPortfolio p, Fund f) {
        PortfolioHolding h = new PortfolioHolding();
        h.setClientPortfolio(p);
        h.setFund(f);
        return h;
    }

    private PortfolioRecommendation createRec(ClientPortfolio p, String name, String reason, String action, String change, String val, String color) {
        PortfolioRecommendation r = new PortfolioRecommendation();
        r.setClientPortfolio(p);
        r.setStockName(name);
        r.setReason(reason);
        r.setAction(action);
        r.setChangePercentage(change);
        r.setCurrentValue(val);
        r.setIconColor(color);
        return r;
    }
}
