package com.manilogix.modulith.supplier;
import com.manilogix.modulith.auth.CurrentUser;
import com.manilogix.modulith.supplier.internal.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class SupplierDirectory {
 private final SupplierRepository suppliers; private final CurrentUser user;
 public SupplierDirectory(SupplierRepository suppliers, CurrentUser user) { this.suppliers=suppliers; this.user=user; }
 public void requireActive(long supplierId) {
  if (!suppliers.existsByIdAndActiveTrue(supplierId)) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Supplier is not active");
 }
 public void requireOwnerOrStaff(long supplierId) {
  if (user.hasRole("ADMIN") || user.hasRole("MANAGER")) return;
  if (!user.hasRole("SUPPLIER") || !suppliers.existsByIdAndOwnerUserIdAndActiveTrue(supplierId,user.id()))
   throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Supplier access denied");
 }
}

