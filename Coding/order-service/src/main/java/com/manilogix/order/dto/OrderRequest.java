package com.manilogix.order.dto;

import java.util.List;

public class OrderRequest {
    private String customerId;
    private List<String> itemIds;
    private String supplierId;
    private String deliveryAddress;

    public OrderRequest() {}

    public OrderRequest(String customerId, List<String> itemIds, String supplierId, String deliveryAddress) {
        this.customerId = customerId;
        this.itemIds = itemIds;
        this.supplierId = supplierId;
        this.deliveryAddress = deliveryAddress;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public List<String> getItemIds() { return itemIds; }
    public void setItemIds(List<String> itemIds) { this.itemIds = itemIds; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
