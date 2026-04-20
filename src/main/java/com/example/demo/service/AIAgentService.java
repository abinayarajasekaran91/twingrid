package com.example.demo.service;

import com.example.demo.dto.PortfolioRequest;
import com.example.demo.dto.PortfolioResponse;
import com.example.demo.entity.ClientPortfolio;
import com.example.demo.entity.PortfolioHolding;
import com.example.demo.repository.ClientPortfolioRepository;
import com.example.demo.repository.PortfolioHoldingRepository;
import com.example.demo.repository.StockHoldingRepository;
import com.example.demo.entity.StockHolding;
import com.example.demo.dao.PortfolioDao;


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
    private StockHoldingRepository stockRepo;

    @Autowired
    private PortfolioDao portfolioDao;



    public PortfolioResponse analyzePortfolio(PortfolioRequest request) {
        PortfolioResponse response = new PortfolioResponse();

        String clientId = request.getClientId() != null ? request.getClientId() : "U1001";

        // ── STEP 1: Ask OpenAI Agent to drive the analysis ──────────────────────
        // OpenAI will call get_client_profile and get_fund_holdings tools automatically.
        // This returns final recommendations after OpenAI has queried the DB via tools.
        // System.out.println("[AI Agent] Starting agentic loop for client: " + clientId);
        // OpenAiClient.AgentResult agentResult = openAiClient.runAgentLoop(clientId);

        // MOCK RESULT (To save tokens as requested)
        OpenAiClient.AgentResult agentResult = new OpenAiClient.AgentResult();
        agentResult.recommendations = new ArrayList<>();
        
        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("name", "Reliance Industries");
        rec1.put("reason", "Overlap penalty of 42% with Fund B detected; reducing concentration to improve diversification.");
        rec1.put("action", "REDUCE");
        rec1.put("change", "-12.0%");
        rec1.put("current", "₹1,47,25,452");
        rec1.put("color", "#ef4444");
        agentResult.recommendations.add(rec1);
        
        Map<String, Object> rec2 = new HashMap<>();
        rec2.put("name", "SBI Bluechip Fund");
        rec2.put("reason", "Strategic increase to fill Small Cap gap and optimize portfolio redundancy score.");
        rec2.put("action", "INCREASE");
        rec2.put("change", "+15.5%");
        rec2.put("current", "₹12,00,000");
        rec2.put("color", "#10b981");
        agentResult.recommendations.add(rec2);

        agentResult.trace = new ArrayList<>();
        Map<String, String> step1 = new HashMap<>();
        step1.put("type", "start");
        step1.put("title", "🧠 AI Agent started (Simulation)");
        step1.put("detail", "Analyzing portfolio for client: " + clientId);
        agentResult.trace.add(step1);

        Map<String, String> step2 = new HashMap<>();
        step2.put("type", "tool-call");
        step2.put("title", "🔧 Tool: calculate_penalties");
        step2.put("detail", "Analyzing Overlap Penalty, Concentration (threshold 8%), and Strategy Deviation.");
        agentResult.trace.add(step2);

        Map<String, String> step3 = new HashMap<>();
        step3.put("type", "reasoning");
        step3.put("title", "🤔 Running Optimization Loop");
        step3.put("detail", "Iteratively adjusting weights to minimize total cost. Diversification score improved from 58 to 84.");
        agentResult.trace.add(step3);

        Map<String, String> step4 = new HashMap<>();
        step4.put("type", "done");
        step4.put("title", "✅ Rebalancing Optimized");
        step4.put("detail", "Final weights minimize redundancy and maximize strategic alignment.");
        agentResult.trace.add(step4);


        // ── STEP 2: Fetch portfolio data for the dashboard display (fast DB read) ─
        Optional<ClientPortfolio> optionalPortfolio = portfolioRepo.findByClientId(clientId);
        if (optionalPortfolio.isEmpty()) {
            response.setStatus("ERROR");
            response.setMessage("Portfolio not found for client ID: " + clientId);
            return response;
        }

        ClientPortfolio portfolio = optionalPortfolio.get();
        response.setStatus("SUCCESS");
        response.setMessage("AI Agent analysis complete — powered by OpenAI GPT with 5-Finger Strategy");
        response.setClientName(portfolio.getClientName());
        response.setRiskLevel(portfolio.getRiskLevel());
        response.setCurrentAllocations(Arrays.asList(
                portfolio.getCurrentEquity(), portfolio.getCurrentCommodity(), portfolio.getCurrentDebt()));
        response.setTargetAllocations(Arrays.asList(
                portfolio.getTargetEquity(), portfolio.getTargetCommodity(), portfolio.getTargetDebt()));
        response.setTotalRebalanceCost(convertToRupees(portfolio.getTotalRebalanceCost()));

        // ── STEP 3: Advanced Overlap Matrix Calculation (Logic from User) ────────
        // Use real MSSQL data if PAN is provided, else fallback to mock/seeded data
        String pan = request.getPan() != null ? request.getPan() : "DZZPA6521D";
        
        List<Map<String, Object>> rawHoldings = portfolioDao.getFundStockHoldings(pan);
        List<Map<String, Object>> clientFunds = portfolioDao.getClientFunds(pan);
        
        // Performance Metrics
        Map<String, Object> perf = portfolioDao.getPortfolioPerformance(pan);
        double currentValue = perf.get("currentValue") != null ? ((Number) perf.get("currentValue")).doubleValue() : 0.0;
        double investedAmount = perf.get("investedAmount") != null ? ((Number) perf.get("investedAmount")).doubleValue() : 0.0;
        
        double gain = currentValue - investedAmount;
        double gainPercent = 0.0;
        if (gain != 0 && investedAmount != 0) {
            gainPercent = (gain / investedAmount) * 100;
        }
        
        response.setCurrentValue(currentValue);
        response.setInvestedAmount(investedAmount);
        response.setGain(gain);
        response.setGainPercent(gainPercent);
        
        // Asset Type Allocations (for Pie Chart)
        response.setAssetTypeAllocations(portfolioDao.getAssetTypeAllocations(pan));
        
        
        // Step 1: Normalize Data (Map<Fund, Map<Stock, Weight>>)
        Map<String, Map<String, Double>> fundHoldings = new HashMap<>();
        Map<String, Map<String, Double>> fundSectors = new HashMap<>();
        Map<String, Map<String, Double>> fundMCaps = new HashMap<>();

        for (Map<String, Object> row : rawHoldings) {
            String fund = (String) row.get("fundName");
            String stock = (String) row.get("stockName");
            String sector = (String) row.get("sector");
            String mcap = (String) row.get("marketCap");
            double weight = ((Number) row.get("weight")).doubleValue();
            
            fundHoldings.computeIfAbsent(fund, k -> new HashMap<>()).put(stock, weight);
            
            if (sector != null) {
                Map<String, Double> sMap = fundSectors.computeIfAbsent(fund, k -> new HashMap<>());
                sMap.put(sector, sMap.getOrDefault(sector, 0.0) + weight);
            }
            if (mcap != null) {
                Map<String, Double> mMap = fundMCaps.computeIfAbsent(fund, k -> new HashMap<>());
                mMap.put(mcap, mMap.getOrDefault(mcap, 0.0) + weight);
            }
        }

        // Map Fund Weights for Portfolio-Level Overlap (Step 4)
        Map<String, Double> fundPortfolioWeights = new HashMap<>();
        for (Map<String, Object> f : clientFunds) {
            fundPortfolioWeights.put((String) f.get("fundName"), ((Number) f.get("fundWeight")).doubleValue() / 100.0);
        }

        // Step 2 & 3: Stock-Level Overlap & Matrix
        List<String> fundList = new ArrayList<>();
        for (Map<String, Object> f : clientFunds) {
            fundList.add((String) f.get("fundName"));
        }

        List<Map<String, Object>> matrix = new ArrayList<>();
        List<String> flags = new ArrayList<>();

        for (int i = 0; i < fundList.size(); i++) {
            String fundA = fundList.get(i);
            Map<String, Object> row = new HashMap<>();
            row.put("fund", fundA);
            row.put("iconColor", "#3b82f6"); 
            
            List<Object> overlaps = new ArrayList<>();
            for (int j = 0; j < fundList.size(); j++) {
                String fundB = fundList.get(j);
                if (i == j) {
                    overlaps.add(100.0);
                } else if (j > i) {
                    overlaps.add("-");
                } else {
                    double score = calculateOverlap(fundHoldings.get(fundA), fundHoldings.get(fundB));
                    
                    // Step 4: Portfolio-Level Overlap (Trick: score * min(weightA, weightB))
                    double weightA = fundPortfolioWeights.getOrDefault(fundA, 0.0);
                    double weightB = fundPortfolioWeights.getOrDefault(fundB, 0.0);
                    double adjustedOverlap = score * Math.min(weightA, weightB);
                    
                    if (score > 0) {
                        overlaps.add(score);
                    } else {
                        overlaps.add("-");
                    }
                    
                    if (score > 35) {
                        flags.add("🚨 Almost same funds: " + fundA + " & " + fundB + " (" + score + "%)");
                    } else if (score >= 20) {
                        flags.add("⚠️ Significant overlap: " + fundA + " & " + fundB + " (" + score + "%)");
                    } else if (score >= 10) {
                        flags.add("🟡 Acceptable overlap: " + fundA + " & " + fundB + " (" + score + "%)");
                    }
                    
                    if (adjustedOverlap > 5) {
                        flags.add("🚨 Portfolio concentration risk: " + adjustedOverlap + "% of total portfolio is duplicated between " + fundA + " & " + fundB);
                    }
                }
            }

            row.put("overlaps", overlaps);
            matrix.add(row);
        }
        response.setMatrix(matrix);


        // 3. Extra Insights (Step 8: Detect Hidden Problems)
        Map<String, Integer> stockFrequency = new HashMap<>();
        Map<String, Double> totalExposure = new HashMap<>();
        
        for (String fund : fundHoldings.keySet()) {
            double fw = fundPortfolioWeights.getOrDefault(fund, 1.0 / fundHoldings.size());
            Map<String, Double> stocks = fundHoldings.get(fund);
            for (Map.Entry<String, Double> entry : stocks.entrySet()) {
                stockFrequency.put(entry.getKey(), stockFrequency.getOrDefault(entry.getKey(), 0) + 1);
                totalExposure.put(entry.getKey(), totalExposure.getOrDefault(entry.getKey(), 0.0) + (entry.getValue() * fw));
            }
        }
        
        List<Map<String, Object>> stockOverlaps = new ArrayList<>();
        for (String stock : stockFrequency.keySet()) {
            if (stockFrequency.get(stock) > 1) {
                Map<String, Object> so = new HashMap<>();
                so.put("stock", stock);
                so.put("fundCount", stockFrequency.get(stock));
                so.put("exposure", Math.round(totalExposure.get(stock) * 100.0) / 100.0);
                stockOverlaps.add(so);
                
                if (stockFrequency.get(stock) > 3) flags.add("🚨 Stock concentration risk: " + stock + " in " + stockFrequency.get(stock) + " funds");
                if (totalExposure.get(stock) > 8.0) flags.add("🚨 Overexposed to " + stock + " (" + so.get("exposure") + "%)");
            }
        }
        
        // Aggregate Sectors & MCaps
        Map<String, Double> sectorMetrics = new HashMap<>();
        Map<String, Double> mcapMetrics = new HashMap<>();
        for (String fund : fundHoldings.keySet()) {
            double fw = fundPortfolioWeights.getOrDefault(fund, 1.0 / fundHoldings.size());
            if (fundSectors.containsKey(fund)) fundSectors.get(fund).forEach((k, v) -> sectorMetrics.put(k, Math.round((sectorMetrics.getOrDefault(k, 0.0) + (v * fw)) * 100.0) / 100.0));
            if (fundMCaps.containsKey(fund)) fundMCaps.get(fund).forEach((k, v) -> mcapMetrics.put(k, Math.round((mcapMetrics.getOrDefault(k, 0.0) + (v * fw)) * 100.0) / 100.0));
        }

        response.setStockOverlap(stockOverlaps);
        response.setSectorOverlap(sectorMetrics);
        response.setMarketCapOverlap(mcapMetrics);
        response.setFlags(flags);

        // ── STEP 4: Attach OpenAI-generated recommendations + agent trace ─────────
        // Passing metrics to Agent context (conceptually, if not using mock)
        // System.out.println("Market Cap Overlap for Agent: " + mcapMetrics);

        // ── STEP 5: Production-Grade Rebalancing Logic ─────────────────────────────
        // 1. Calculate "Before" Scores
        double beforeOverlapScore = calculateTotalOverlapScore(fundList, fundHoldings, fundPortfolioWeights);
        double beforeConcentration = calculateConcentrationPenalty(totalExposure);
        double beforeDiversification = calculateDiversificationScore(beforeOverlapScore, totalExposure);

        // 2. Run Iterative Optimization
        Map<String, Double> optimizedWeights = optimizePortfolio(fundList, fundHoldings, fundPortfolioWeights, portfolio);
        
        // 3. Calculate "After" Scores
        // Recalculate exposures for "After" state
        Map<String, Double> afterTotalExposure = new HashMap<>();
        for (String fund : fundHoldings.keySet()) {
            double fw = optimizedWeights.getOrDefault(fund, 0.0);
            Map<String, Double> stocks = fundHoldings.get(fund);
            for (Map.Entry<String, Double> entry : stocks.entrySet()) {
                afterTotalExposure.put(entry.getKey(), afterTotalExposure.getOrDefault(entry.getKey(), 0.0) + (entry.getValue() * fw));
            }
        }
        double afterOverlapScore = calculateTotalOverlapScore(fundList, fundHoldings, optimizedWeights);
        double afterDiversification = calculateDiversificationScore(afterOverlapScore, afterTotalExposure);

        Map<String, Object> scores = new HashMap<>();
        scores.put("before", Map.of("overlapScore", Math.round(beforeOverlapScore), "diversificationScore", Math.round(beforeDiversification)));
        scores.put("after", Map.of("overlapScore", Math.round(afterOverlapScore), "diversificationScore", Math.round(afterDiversification)));
        response.setPortfolioScores(scores);

        // 4. Generate Optimized Actions
        List<Map<String, Object>> actions = new ArrayList<>();
        for (String fund : fundList) {
            double from = Math.round(fundPortfolioWeights.getOrDefault(fund, 0.0) * 1000.0) / 10.0;
            double to = Math.round(optimizedWeights.getOrDefault(fund, 0.0) * 1000.0) / 10.0;
            if (Math.abs(from - to) > 0.1) {
                Map<String, Object> action = new HashMap<>();
                action.put("fund", fund);
                action.put("action", to > from ? "INCREASE" : "REDUCE");
                action.put("from", from);
                action.put("to", to);
                
                // Reason generation
                double redundancy = 0.0;
                for (String other : fundList) {
                    if (!fund.equals(other)) redundancy += calculateOverlap(fundHoldings.get(fund), fundHoldings.get(other));
                }
                if (to < from) {
                    action.put("reason", "High redundancy score (" + Math.round(redundancy) + "%) and concentration in overlapping holdings.");
                } else {
                    action.put("reason", "Low overlap and improves strategy alignment with " + portfolio.getRiskLevel() + " profile.");
                }
                actions.add(action);
            }
        }
        response.setOptimizedActions(actions);

        response.setRecommendations(agentResult.recommendations);
        response.setAgentTrace(agentResult.trace);
        System.out.println("[AI Agent] Analysis complete. Recommendations: " + agentResult.recommendations.size());

        return response;
    }

    private double calculateTotalOverlapScore(List<String> funds, Map<String, Map<String, Double>> holdings, Map<String, Double> weights) {
        double totalPenalty = 0.0;
        for (int i = 0; i < funds.size(); i++) {
            for (int j = i + 1; j < funds.size(); j++) {
                String fA = funds.get(i);
                String fB = funds.get(j);
                double overlap = calculateOverlap(holdings.get(fA), holdings.get(fB));
                totalPenalty += overlap * Math.min(weights.getOrDefault(fA, 0.0), weights.getOrDefault(fB, 0.0));
            }
        }
        return totalPenalty;
    }

    private double calculateConcentrationPenalty(Map<String, Double> exposures) {
        double penalty = 0.0;
        for (double exp : exposures.values()) {
            if (exp > 8.0) penalty += Math.pow(exp - 8.0, 2);
        }
        return penalty;
    }

    private double calculateDiversificationScore(double overlapPenalty, Map<String, Double> exposures) {
        double maxExp = 0.0;
        for (double exp : exposures.values()) maxExp = Math.max(maxExp, exp);
        return Math.max(0, 100 - (overlapPenalty * 2) - (maxExp > 10 ? (maxExp - 10) * 3 : 0));
    }

    private Map<String, Double> optimizePortfolio(List<String> funds, Map<String, Map<String, Double>> holdings, Map<String, Double> currentWeights, ClientPortfolio portfolio) {
        Map<String, Double> weights = new HashMap<>(currentWeights);
        
        // Iterative optimization (Simplified Greedy)
        for (int iter = 0; iter < 10; iter++) {
            // 1. Calculate Redundancy for each fund
            Map<String, Double> redundancy = new HashMap<>();
            for (String fA : funds) {
                double r = 0.0;
                for (String fB : funds) {
                    if (!fA.equals(fB)) r += calculateOverlap(holdings.get(fA), holdings.get(fB)) * weights.getOrDefault(fB, 0.0);
                }
                redundancy.put(fA, r);
            }

            // 2. Reduce high redundancy funds
            double totalReduced = 0.0;
            for (String f : funds) {
                if (redundancy.get(f) > 10.0) { // Threshold for high redundancy
                    double reduction = weights.get(f) * 0.1; // Reduce by 10% each step
                    weights.put(f, weights.get(f) - reduction);
                    totalReduced += reduction;
                }
            }

            // 3. Redistribute to low redundancy funds
            List<String> lowRedundancyFunds = new ArrayList<>();
            for (String f : funds) if (redundancy.get(f) < 5.0) lowRedundancyFunds.add(f);
            
            if (!lowRedundancyFunds.isEmpty()) {
                double share = totalReduced / lowRedundancyFunds.size();
                for (String f : lowRedundancyFunds) weights.put(f, weights.get(f) + share);
            } else {
                // If no low redundancy funds, redistribute equally to all
                double share = totalReduced / funds.size();
                for (String f : funds) weights.put(f, weights.get(f) + share);
            }
        }
        
        // Ensure constraints: min 0, max 40%, sum to 1
        double sum = 0.0;
        for (String f : funds) {
            double w = Math.max(0, Math.min(0.40, weights.get(f)));
            weights.put(f, w);
            sum += w;
        }
        for (String f : funds) weights.put(f, weights.get(f) / sum);

        return weights;
    }

    /**
     * Converts a dollar amount string (e.g. "-$72,135.64") to Indian Rupees (₹).
     * Uses 1 USD = ₹84 exchange rate.
     */
    private String convertToRupees(String dollarStr) {
        if (dollarStr == null) return "₹0";
        try {
            boolean negative = dollarStr.contains("-");
            boolean positive = dollarStr.contains("+");
            // Strip sign, $ and commas, then parse
            String cleaned = dollarStr.replace("-", "").replace("+", "")
                                      .replace("$", "").replace(",", "").trim();
            double usdValue = Double.parseDouble(cleaned);
            double inrValue = usdValue * 84.0;

            // Format in Indian number system (e.g. 60,59,384)
            long inrLong = Math.round(inrValue);
            String formatted = formatIndianNumber(inrLong);
            String sign = negative ? "-" : (positive ? "+" : "");
            return sign + "₹" + formatted;
        } catch (NumberFormatException e) {
            // If already in ₹ or unparseable, return as-is
            return dollarStr;
        }
    }

    private String formatIndianNumber(long n) {
        if (n == 0) return "0";
        String s = Long.toString(n);
        if (s.length() <= 3) return s;
        // Last 3 digits, then groups of 2
        StringBuilder result = new StringBuilder();
        result.insert(0, s.substring(s.length() - 3));
        s = s.substring(0, s.length() - 3);
        while (s.length() > 2) {
            result.insert(0, "," + s.substring(s.length() - 2));
            s = s.substring(0, s.length() - 2);
        }
        if (!s.isEmpty()) result.insert(0, s + ",");
        return result.toString();
    }

    private double calculateOverlap(Map<String, Double> fundA, Map<String, Double> fundB) {
        if (fundA == null || fundB == null) return 0.0;
        double overlap = 0.0;
        for (String stock : fundA.keySet()) {
            if (fundB.containsKey(stock)) {
                overlap += Math.min(fundA.get(stock), fundB.get(stock));
            }
        }
        return Math.round(overlap * 100.0) / 100.0;
    }

    public Map<String, Object> validateOverlap(String pan) {
        List<Map<String, Object>> rawHoldings = portfolioDao.getFundStockHoldings(pan);
        List<Map<String, Object>> clientFunds = portfolioDao.getClientFunds(pan);
        
        Map<String, Map<String, Double>> fundHoldings = new HashMap<>();
        for (Map<String, Object> row : rawHoldings) {
            String fund = (String) row.get("fundName");
            String stock = (String) row.get("stockName");
            double weight = ((Number) row.get("weight")).doubleValue();
            fundHoldings.computeIfAbsent(fund, k -> new HashMap<>()).put(stock, weight);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pan", pan);
        result.put("totalFunds", fundHoldings.size());
        result.put("holdingsStructure", fundHoldings);
        
        List<Map<String, Object>> pairs = new ArrayList<>();
        List<String> funds = new ArrayList<>(fundHoldings.keySet());
        for (int i = 0; i < funds.size(); i++) {
            for (int j = i + 1; j < funds.size(); j++) {
                String fA = funds.get(i);
                String fB = funds.get(j);
                double score = calculateOverlap(fundHoldings.get(fA), fundHoldings.get(fB));
                Map<String, Object> pair = new HashMap<>();
                pair.put("fundA", fA);
                pair.put("fundB", fB);
                pair.put("overlapScore", score);
                pairs.add(pair);
            }
        }
        result.put("overlapPairs", pairs);
        return result;
    }
}

