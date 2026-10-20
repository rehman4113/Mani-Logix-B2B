package com.manilogix.modulith.supplier.internal;
import com.manilogix.modulith.supplier.SupplierDirectory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/suppliers")
class SupplierController {
 private final SupplierRepository repository; private final SupplierDirectory directory; private final com.manilogix.modulith.auth.AuthDirectory accounts;
 SupplierController(SupplierRepository repository,SupplierDirectory directory,com.manilogix.modulith.auth.AuthDirectory accounts) {this.repository=repository;this.directory=directory;this.accounts=accounts;}
 record Input(@NotBlank String name,@NotBlank @Email String email,@NotNull @Positive Long ownerUserId) {}
 record View(long id,String name,String email,long ownerUserId,boolean active,Instant createdAt) {}
 private View view(Supplier s) {return new View(s.id,s.name,s.email,s.ownerUserId,s.active,s.createdAt);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
 View create(@Valid @RequestBody Input input) { accounts.requireSupplier(input.ownerUserId()); return view(repository.save(new Supplier(input.name(),input.email(),input.ownerUserId()))); }
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
 List<View> all() {return repository.findAll().stream().map(this::view).toList();}
 @GetMapping("/{id}") View one(@PathVariable long id) {
  directory.requireOwnerOrStaff(id);
  return view(repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
 }
 @PatchMapping("/{id}") @Transactional
 View update(@PathVariable long id,@Valid @RequestBody Input input) {
  directory.requireOwnerOrStaff(id);
  Supplier s=repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  s.name=input.name();s.email=input.email();
  // Ownership is never changed by a supplier updating their profile.
  return view(s);
 }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @Transactional
 void deactivate(@PathVariable long id) {
  Supplier s=repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));s.active=false;
 }
}

