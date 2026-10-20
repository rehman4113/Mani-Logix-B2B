package com.manilogix.modulith.inventory.internal;
import com.manilogix.modulith.auth.CurrentUser;
import com.manilogix.modulith.supplier.SupplierDirectory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/inventory")
class InventoryController {
 private final InventoryRepository items;private final SupplierDirectory suppliers;private final CurrentUser user;
 InventoryController(InventoryRepository items,SupplierDirectory suppliers,CurrentUser user) {this.items=items;this.suppliers=suppliers;this.user=user;}
 record Input(@NotNull @Positive Long supplierId,@NotBlank String name,String category,@NotNull @PositiveOrZero Integer quantity,
  @NotNull @PositiveOrZero Integer reorderLevel,@NotNull @DecimalMin("0.00") BigDecimal unitPrice){}
 record View(long id,long supplierId,String name,String category,int quantity,int reorderLevel,BigDecimal unitPrice,boolean active,boolean needsPriceReview){}
 private View view(InventoryItem i) {return new View(i.id,i.supplierId,i.name,i.category,i.quantity,i.reorderLevel,i.unitPrice,i.active,i.needsPriceReview);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
 View create(@Valid @RequestBody Input input) {
  suppliers.requireActive(input.supplierId());suppliers.requireOwnerOrStaff(input.supplierId());
  InventoryItem item=new InventoryItem(input.supplierId(),input.name(),input.quantity(),input.reorderLevel(),input.unitPrice());
  item.category=input.category();return view(items.save(item));
 }
 @GetMapping
 Page<View> all(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
  if(page<0 || size<1 || size>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid page size");
  var request=PageRequest.of(page,size);
  if(user.hasRole("SUPPLIER")) {
   var owned=suppliers.ownedSupplierIds();
   return owned.isEmpty()?Page.empty(request):items.findBySupplierIdInAndActiveTrue(owned,request).map(this::view);
  }
  return items.findByActiveTrue(request).map(this::view);
 }
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
  item.name=input.name();item.category=input.category();item.quantity=input.quantity();item.reorderLevel=input.reorderLevel();item.unitPrice=input.unitPrice();
  if(item.needsPriceReview) {
   if(input.unitPrice().signum()<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Confirm a positive price before enabling imported stock");
   item.needsPriceReview=false;item.active=true;
  }
  return view(item);
 }
 @DeleteMapping("/{id}") @Transactional
 void deactivate(@PathVariable long id) {
  InventoryItem item=items.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  suppliers.requireOwnerOrStaff(item.supplierId);item.active=false;
 }
}

