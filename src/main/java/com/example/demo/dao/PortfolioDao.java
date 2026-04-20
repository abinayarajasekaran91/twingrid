package com.example.demo.dao;

import java.util.List;
import java.util.Map;

public interface PortfolioDao {
    List<Map<String, Object>> getFundStockHoldings(String pan);
    List<Map<String, Object>> getClientFunds(String pan);
    Map<String, Object> getPortfolioPerformance(String pan);
}
