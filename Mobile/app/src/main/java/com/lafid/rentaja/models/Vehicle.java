package com.lafid.rentaja.models;

import java.util.List;

public class Vehicle {
    private String id;
    private String name;
    private String category;
    private long pricePerDay;
    private long deposit;
    private String desc;
    private List<String> specs;
    private String ownerId;
    private String ownerName;
    private String status; // "available", "rented"
    private String imageUrl;

    public Vehicle() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(long pricePerDay) { this.pricePerDay = pricePerDay; }

    public long getDeposit() { return deposit; }
    public void setDeposit(long deposit) { this.deposit = deposit; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public List<String> getSpecs() { return specs; }
    public void setSpecs(List<String> specs) { this.specs = specs; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return "available".equals(status); }

    public String getCategoryEmoji() {
        if (category == null) return "🚗";
        switch (category.toLowerCase()) {
            case "motor": return "🛵";
            case "kapal": return "🚢";
            case "alutsista": return "🎖️";
            case "truk": return "🚛";
            case "bus": return "🚌";
            default: return "🚗";
        }
    }
}
