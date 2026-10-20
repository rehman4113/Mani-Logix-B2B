package com.manilogix.modulith.inventory;
import com.manilogix.modulith.inventory.internal.InventoryItem;
import com.manilogix.modulith.inventory.internal.InventoryRepository;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service
public class InventoryCatalog {
 private final InventoryRepository items;
 public InventoryCatalog(InventoryRepository items) {this.items=items;}
 public record ReservedItem(long itemId,long supplierId,int quantity,BigDecimal unitPrice) {}
 @Transactional
 public ReservedItem reserve(long id,int quantity) {
  if(quantity<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity must be positive");
  InventoryItem item=items.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Unknown item"));
  if(!item.active || item.quantity<quantity) throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock");
  item.quantity-=quantity;
  return new ReservedItem(item.id,item.supplierId,quantity,item.unitPrice);
 }
 @Transactional
 public void restock(long id,int quantity) {
  InventoryItem item=items.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,"Item missing for restock"));
  item.quantity+=quantity;
 }
}

