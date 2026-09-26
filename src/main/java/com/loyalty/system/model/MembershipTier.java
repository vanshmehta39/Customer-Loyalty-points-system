package com.loyalty.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "membership_tiers")
public class MembershipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer minPoints;

    private Integer maxPoints; // null or high value for Diamond

    @Column(nullable = false)
    private Double discountPercentage = 0.0;

    @Column(nullable = false)
    private Boolean freeDelivery = false;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    private String badgeColor;
    private String icon;

    public MembershipTier() {
    }

    public MembershipTier(String name, Integer minPoints, Integer maxPoints, Double discountPercentage, Boolean freeDelivery, String benefits, String badgeColor, String icon) {
        this.name = name;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.discountPercentage = discountPercentage;
        this.freeDelivery = freeDelivery;
        this.benefits = benefits;
        this.badgeColor = badgeColor;
        this.icon = icon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMinPoints() { return minPoints; }
    public void setMinPoints(Integer minPoints) { this.minPoints = minPoints; }

    public Integer getMaxPoints() { return maxPoints; }
    public void setMaxPoints(Integer maxPoints) { this.maxPoints = maxPoints; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Boolean getFreeDelivery() { return freeDelivery; }
    public void setFreeDelivery(Boolean freeDelivery) { this.freeDelivery = freeDelivery; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
