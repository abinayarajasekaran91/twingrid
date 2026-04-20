package com.example.demo.repository;

import com.example.demo.entity.PortfolioRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRecommendationRepository extends JpaRepository<PortfolioRecommendation, Long> {
    List<PortfolioRecommendation> findByClientPortfolioId(Long clientPortfolioId);
}
