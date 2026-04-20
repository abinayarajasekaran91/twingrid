package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "funds")
public class Fund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    
    // Used for the Matrix
    private int meticulousOverlap;
    private int riskOverlap;
    private int meatOverlap;
    private String iconColor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getMeticulousOverlap() { return meticulousOverlap; }
    public void setMeticulousOverlap(int meticulousOverlap) { this.meticulousOverlap = meticulousOverlap; }

    public int getRiskOverlap() { return riskOverlap; }
    public void setRiskOverlap(int riskOverlap) { this.riskOverlap = riskOverlap; }

    public int getMeatOverlap() { return meatOverlap; }
    public void setMeatOverlap(int meatOverlap) { this.meatOverlap = meatOverlap; }

    public String getIconColor() { return iconColor; }
    public void setIconColor(String iconColor) { this.iconColor = iconColor; }
}
