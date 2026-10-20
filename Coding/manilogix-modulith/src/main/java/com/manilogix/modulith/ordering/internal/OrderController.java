package com.manilogix.modulith.ordering.internal;
import com.manilogix.modulith.auth.CurrentUser;
import com.manilogix.modulith.inventory.InventoryCatalog;
import com.manilogix.modulith.ordering.OrderPlaced;
import com.manilogix.modulith.supplier.SupplierDirectory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/orders")
class OrderController {
 private final PurchaseOrderRepository orders;private final InventoryCatalog stock;
 private final SupplierDirectory suppliers;private final CurrentUser user;private final ApplicationEventPublisher events;
 OrderController(PurchaseOrderRepository orders,InventoryCatalog stock,SupplierDirectory suppliers,CurrentUser user,ApplicationEventPublisher events) {
  this.orders=orders;this.stock=stock;this.suppliers=suppliers;this.user=user;this.events=events;
 }
 record Item(@NotNull @Positive Long itemId,@Positive int quantity) {}
 record Input(@NotEmpty List<@Valid Item> items,@NotBlank String deliveryAddress){}
 record LineView(long itemId,int quantity,BigDecimal unitPrice){}
 record View(long id,long customerId,long supplierId,String deliveryAddress,PurchaseOrder.Status status,Instant createdAt,BigDecimal total,List<LineView> items){}
 private View view(PurchaseOrder o) {
  return new View(o.id,o.customerId,o.supplierId,o.deliveryAddress,o.status,o.createdAt,o.total,
   o.lines.stream().map(l->new LineView(l.itemId,l.quantity,l.unitPrice)).toList());
 }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('CUSTOMER')") @Transactional
 View place(@Valid @RequestBody Input input) {
  if(input.items().stream().map(Item::itemId).distinct().count()!=input.items().size())
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Duplicate items");
  List<Item> requested=new ArrayList<>(input.items());
  requested.sort(Comparator.comparing(Item::itemId)); // consistent lock order for concurrent orders
  List<InventoryCatalog.ReservedItem> reserved=new ArrayList<>();
  Long supplierId=null;
  for(Item line:requested) {
   var result=stock.reserve(line.itemId(),line.quantity());
   if(supplierId!=null && supplierId.longValue()!=result.supplierId()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Items must belong to one supplier");
   supplierId=result.supplierId();reserved.add(result);
  }
  suppliers.requireActive(supplierId);
  PurchaseOrder order=new PurchaseOrder(user.id(),supplierId,input.deliveryAddress());
  for(var item:reserved) {
   order.lines.add(new PurchaseOrder.Line(item.itemId(),item.quantity(),item.unitPrice()));
   order.total=order.total.add(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
  }
  PurchaseOrder saved=orders.saveAndFlush(order);
  events.publishEvent(new OrderPlaced(saved.id,saved.customerId));
  return view(saved);
 }
 @GetMapping @Transactional(readOnly=true)
 List<View> list() {
  if(user.hasRole("ADMIN") || user.hasRole("MANAGER")) return orders.findAll().stream().map(this::view).toList();
  if(user.hasRole("SUPPLIER")) {
   var owned=suppliers.ownedSupplierIds();
   return owned.isEmpty()?List.of():orders.findBySupplierIdIn(owned).stream().map(this::view).toList();
  }
  return orders.findByCustomerId(user.id()).stream().map(this::view).toList();
 }
 @GetMapping("/{id}") @Transactional(readOnly=true)
 View one(@PathVariable long id) {
  PurchaseOrder o=orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  access(o);return view(o);
 }
 private void access(PurchaseOrder o) {
  if(user.hasRole("ADMIN") || user.hasRole("MANAGER") || o.customerId==user.id()) return;
  suppliers.requireOwnerOrStaff(o.supplierId);
 }
 @PatchMapping("/{id}/status") @Transactional
 View status(@PathVariable long id,@RequestParam PurchaseOrder.Status status) {
  PurchaseOrder o=orders.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  access(o);
  if(status==PurchaseOrder.Status.CANCELLED) {
   if(o.status!=PurchaseOrder.Status.PENDING && o.status!=PurchaseOrder.Status.CONFIRMED)
    throw new ResponseStatusException(HttpStatus.CONFLICT,"Order can no longer be cancelled");
   o.lines.stream().sorted(Comparator.comparing(l->l.itemId)).forEach(l -> stock.restock(l.itemId,l.quantity));
  } else {
   suppliers.requireOwnerOrStaff(o.supplierId);
   boolean allowed=(o.status==PurchaseOrder.Status.PENDING && status==PurchaseOrder.Status.CONFIRMED)
    ||(o.status==PurchaseOrder.Status.CONFIRMED && status==PurchaseOrder.Status.SHIPPED)
    ||(o.status==PurchaseOrder.Status.SHIPPED && status==PurchaseOrder.Status.DELIVERED);
   if(!allowed) throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid order transition");
  }
  o.status=status;return view(o);
 }
}

