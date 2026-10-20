package com.manilogix.modulith.inventory.internal;
import com.manilogix.modulith.auth.CurrentUser;
import com.manilogix.modulith.supplier.SupplierDirectory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/inventory")
class InventoryController {
 private final InventoryRepository items;private final SupplierDirectory suppliers;private final CurrentUser user;
 InventoryController(InventoryRepository items,SupplierDirectory suppliers,CurrentUser user) {this.items=items;this.suppliers=suppliers;this.user=user;}
 record Input(@NotNull @Positive Long supplierId,@NotBlank String name,@NotNull @PositiveOrZero Integer quantity,
  @NotNull @PositiveOrZero Integer reorderLevel,@NotNull @DecimalMin("0.00") BigDecimal unitPrice){}
 record View(long id,long supplierId,String name,int quantity,int reorderLevel,BigDecimal unitPrice,boolean active){}
 private View view(InventoryItem i) {return new View(i.id,i.supplierId,i.name,i.quantity,i.reorderLevel,i.unitPrice,i.active);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
 View create(@Valid @RequestBody Input input) {
  suppliers.requireActive(input.supplierId());suppliers.requireOwnerOrStaff(input.supplierId());
  return view(items.save(new InventoryItem(input.supplierId(),input.name(),input.quantity(),input.reorderLevel(),input.unitPrice())));
 }
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
 List<View> all() {return items.findAll().stream().filter(i->i.active).map(this::view).toList();}
 @GetMapping("/{id}") View one(@PathVariable long id) {
  InventoryItem item=items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  if (!item.active) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  // Customers may inspect listed items; suppliers may inspect only their own items.
  if (user.hasRole("SUPPLIER")) suppliers.requireOwnerOrStaff(item.supplierId);
  return view(item);
 }
 @PutMapping("/{id}") @Transactional
 View update(@PathVariable long id,@Valid @RequestBody Input input) {
  InventoryItem item=items.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  suppliers.requireOwnerOrStaff(item.supplierId);
  if (!item.supplierId.equals(input.supplierId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supplier cannot be changed");
  item.name=input.name();item.quantity=input.quantity();item.reorderLevel=input.reorderLevel();item.unitPrice=input.unitPrice();
  return view(item);
 }
 @DeleteMapping("/{id}") @Transactional
 void deactivate(@PathVariable long id) {
  InventoryItem item=items.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  suppliers.requireOwnerOrStaff(item.supplierId);item.active=false;
 }
}

