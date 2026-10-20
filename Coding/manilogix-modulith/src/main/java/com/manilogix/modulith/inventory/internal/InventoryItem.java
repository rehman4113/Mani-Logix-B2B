package com.manilogix.modulith.inventory.internal;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="inventory_items")
public class InventoryItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long supplierId;
 @Column(nullable=false) public String name;
 @Column(nullable=false) public int quantity;
 @Column(nullable=false) public int reorderLevel;
 @Column(nullable=false,precision=15,scale=2) public BigDecimal unitPrice;
 @Column(nullable=false) public boolean active=true;
 protected InventoryItem(){}
 InventoryItem(long supplierId,String name,int quantity,int reorderLevel,BigDecimal unitPrice) {
  this.supplierId=supplierId;this.name=name;this.quantity=quantity;this.reorderLevel=reorderLevel;this.unitPrice=unitPrice;
 }
}

