package com.example.demo.repository;

import com.example.demo.entity.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByFundId(Long fundId);
}
