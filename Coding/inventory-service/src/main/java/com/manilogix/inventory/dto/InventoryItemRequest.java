package com.manilogix.inventory.dto;

public class InventoryItemRequest {
    private String name;
    private String category;
    private int quantity;
    private String supplierId;
    private int reorderLevel;

    public InventoryItemRequest() {
    }

    public InventoryItemRequest(String name, String category, int quantity, String supplierId, int reorderLevel) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.supplierId = supplierId;
        this.reorderLevel = reorderLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
}
