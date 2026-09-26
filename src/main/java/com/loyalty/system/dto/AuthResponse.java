package com.loyalty.system.dto;

public class AuthResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private Integer pointsBalance;
    private String tierName;
    private String badgeColor;
    private String token; // simple session token (or email-based bearer identifier)

    public AuthResponse() {}

    public AuthResponse(Long id, String fullName, String email, String role, Integer pointsBalance, String tierName, String badgeColor, String token) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.pointsBalance = pointsBalance;
        this.tierName = tierName;
        this.badgeColor = badgeColor;
        this.token = token;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(Integer pointsBalance) { this.pointsBalance = pointsBalance; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
