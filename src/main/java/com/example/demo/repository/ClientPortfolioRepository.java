package com.example.demo.repository;

import com.example.demo.entity.ClientPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientPortfolioRepository extends JpaRepository<ClientPortfolio, Long> {
    Optional<ClientPortfolio> findByClientId(String clientId);
}
