package com.manilogix.modulith.ordering.internal;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="purchase_orders")
public class PurchaseOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long customerId;
 @Column(nullable=false) public Long supplierId;
 @Column(nullable=false) public String deliveryAddress;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) public Status status=Status.PENDING;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 @Column(nullable=false,precision=15,scale=2) public BigDecimal total=BigDecimal.ZERO;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="order_lines",joinColumns=@JoinColumn(name="order_id"))
 public List<Line> lines=new ArrayList<>();
 public enum Status {PENDING,CONFIRMED,SHIPPED,DELIVERED,CANCELLED}
 @Embeddable public static class Line {
  @Column(name="item_id",nullable=false) public Long itemId;
  @Column(name="quantity",nullable=false) public int quantity;
  @Column(name="unit_price",nullable=false,precision=15,scale=2) public BigDecimal unitPrice;
  protected Line(){}
  public Line(long itemId,int quantity,BigDecimal unitPrice) {this.itemId=itemId;this.quantity=quantity;this.unitPrice=unitPrice;}
 }
 protected PurchaseOrder(){}
 public PurchaseOrder(long customerId,long supplierId,String deliveryAddress) {
  this.customerId=customerId;this.supplierId=supplierId;this.deliveryAddress=deliveryAddress;
 }
}

